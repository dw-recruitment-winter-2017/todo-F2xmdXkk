(ns democrado.endpoints
  (:require
   [datomic.api :as d]
   [democrado.api :as api]
   [democrado.db :as db]
   [yada.yada :as yada])
  (:import
   [java.util UUID]))

(defn notes-resource [conn]
  (yada/resource {:id :notes-resource
                  :methods
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
  (yada/resource {:id :note-resource
                  :methods
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
