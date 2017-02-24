(ns democrado.server
  (:require
   [byte-streams :as b]
   [clojure.java.io :as io]
   [clojure.walk :refer [keywordize-keys]]
   [datomic.api :as d]
   [democrado.db :as db]
   [integrant.core :as integrant]
   [schema.core :refer [defschema]]
   [yada.yada :as yada]))

(defn notes-resource [conn]
  (yada/resource {:methods
                  {:get
                   {:produces #{"application/json" "application/transit+json" "text/html"}
                    :response
                    (fn [ctx]
                      (db/get-todos (d/db conn)))}
                   :post
                   {:produces #{"application/json" "application/transit+json"}
                    :consumes #{"application/json" "application/transit+json"}
                    :response
                    (fn [{:keys [body] :as ctx}]
                      (db/add-todo! conn (keywordize-keys body)))}}}))

(defn routes [conn]
  [""
   [["/api" [["/notes" (notes-resource conn)]]]
    ["/"    [[""       (yada/as-resource (io/file "target/index.html"))]
             ["about"  (yada/as-resource (io/file "target/index.html"))]
             [""       (yada/as-resource (io/file "target"))]]]
    [true   (yada/handler nil)]]])

(defmethod integrant/init-key :democrado.server/listener [_ {:keys [conn port] :as opts}]
  (yada/listener
   (routes conn)
   {:port port}))

(defmethod integrant/halt-key! :democrado.server/listener [_ listener]
  ((:close listener)))
