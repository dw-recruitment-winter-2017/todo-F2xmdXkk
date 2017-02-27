(ns democrado.server
  (:require
   [byte-streams :as b]
   [clojure.java.io :as io]
   [clojure.walk :refer [keywordize-keys]]
   [datomic.api :as d]
   [democrado.api :as api]
   [democrado.db :as db]
   [integrant.core :as integrant]
   [schema.core :refer [defschema]]
   [yada.yada :as yada])
  (:import
   [java.util UUID]))

(defn notes-resource [conn]
  (yada/resource {:methods
                  {:get
                   {:produces #{"application/json" "application/transit+json" "text/html"}
                    :response
                    (fn [ctx]
                      (into []
                            (map api/read-sanitize-todo)
                            (db/get-todos (d/db conn))))}
                   :post
                   {:produces #{"application/json" "application/transit+json"}
                    :consumes #{"application/json" "application/transit+json"}
                    :response
                    (fn [{:keys [body] :as ctx}]
                      (let [todo (api/write-sanitize-todo body)]
                        (-> (db/add-todo! conn todo)
                            api/read-sanitize-todo)))}}}))

(defn note-resource [conn]
  (yada/resource {:methods
                  {:put
                   {:produces #{"application/json" "application/transit+json"}
                    :consumes #{"application/json" "application/transit+json"}
                    :response
                    (fn [{:keys [body] :as ctx}]
                      (let [todo-id (-> ctx
                                        (get-in [:parameters :path :id])
                                        UUID/fromString)
                            todo (api/write-sanitize-todo body)]
                        (-> (db/update-todo! conn todo-id todo)
                            api/read-sanitize-todo)))}
                   :delete
                   {:produces #{"application/json" "application/transit+json"}
                    :consumes #{"application/json" "application/transit+json"}
                    :response
                    ;; TODO: return something meaningful?
                    (fn [{:keys [body] :as ctx}]
                      (let [todo-id (get-in ctx [:parameters :path :id])]
                        (db/delete-todo! conn (UUID/fromString todo-id))
                        {}))}}}))

(defn routes [conn]
  [""
   [["/api/notes" [[""        (notes-resource conn)]
                   [["/" :id] (note-resource conn)]]]
    ["/"          [[""       (yada/as-resource (io/file "target/index.html"))]
                   ["about"  (yada/as-resource (io/file "target/index.html"))]
                   [""       (yada/as-resource (io/file "target"))]]]
    [true         (yada/handler nil)]]])

(defmethod integrant/init-key :democrado.server/listener [_ {:keys [conn port] :as opts}]
  (yada/listener
   (routes conn)
   {:port port}))

(defmethod integrant/halt-key! :democrado.server/listener [_ listener]
  ((:close listener)))
