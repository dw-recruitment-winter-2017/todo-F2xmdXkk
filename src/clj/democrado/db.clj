(ns democrado.db
  (:require
   [datomic.api :as d]
   [integrant.core :as integrant]))

(def schema [{:db/id #db/id[:db.part/db]
              :db/ident :todo/id
              :db/valueType :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/identity
              :db.install/_attribute :db.part/db}

             {:db/id #db/id[:db.part/db]
              :db/ident :todo/created-at
              :db/valueType :db.type/instant
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/id #db/id[:db.part/db]
              :db/ident :todo/description
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}

             {:db/id #db/id[:db.part/db]
              :db/ident :todo/completed
              :db/valueType :db.type/boolean
              :db/cardinality :db.cardinality/one
              :db.install/_attribute :db.part/db}])

(defn ensure-schema [conn]
  (or (-> conn d/db (d/entid :tx/commit))
      @(d/transact conn schema)))

(defmethod integrant/init-key :democrado.db/conn [_ {:keys [uri] :as opts}]
  (let [_ (d/create-database uri)
        conn (d/connect uri)]
    (ensure-schema conn)
    conn))

(defn add-todo-tx [todo]
  [(assoc todo
          :db/id "new-todo-eid"
          :todo/id (d/squuid)
          :todo/created-at (java.util.Date.))])

;; TODO: move into API layer
(defn db-todo->api-todo [db-todo]
  (dissoc db-todo :db/id))

(defn get-todo [db todo-id]
  (db-todo->api-todo
   (d/q '[:find (pull ?t [*]) .
          :in $ ?id
          :where [?t :todo/id ?id]]
        db todo-id)))

;; TODO: cleanup and simplify
(defn add-todo! [conn todo]
  (let [tx (add-todo-tx todo)
        {:keys [tempids db-after]} @(d/transact conn tx)]
    (->> (d/resolve-tempid db-after tempids "new-todo-eid")
         (d/entity db-after)
         :todo/id
         (get-todo db-after))))

(defn get-todos [db]
  (into []
        (map db-todo->api-todo)
        (d/q '[:find [(pull ?t [*]) ...]
               :where [?t :todo/description]]
             db)))

(defn update-todo-tx [todo-id todo]
  [(-> todo
       (dissoc :db/id :todo/id :todo/created-at)
       (assoc :todo/id todo-id))])

(defn update-todo! [conn todo-id todo]
  (let [tx (update-todo-tx todo-id todo)
        {:keys [db-after]} @(d/transact conn tx)]
    (get-todo db-after todo-id)))

(defn delete-todo! [conn todo-id]
  @(d/transact conn [[:db.fn/retractEntity [:todo/id todo-id]]]))
