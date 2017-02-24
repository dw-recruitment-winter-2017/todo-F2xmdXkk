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

(defn todo-tx [todo]
  [(cond-> (assoc todo :db/id "new-todo-eid")
     (not (:todo/id todo))
     (assoc :todo/id (d/squuid)))])

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
  (let [tx (-> todo (assoc :todo/created-at (java.util.Date.)) todo-tx)
        tx-ret (d/transact conn tx)
        {:keys [tempids db-after]} @tx-ret]
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
