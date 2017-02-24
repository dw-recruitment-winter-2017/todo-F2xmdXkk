(ns democrado.test-helper
  (:require
   [clojure.test :as t]
   [datomic.api :as d]
   [democrado.db :as db]
   [integrant.core :as integrant]))

(def config
  {:democrado.db/conn {:uri "datomic:mem://democrado-test"}})

(declare system)

(defn init-system [f]
  (with-redefs [system (integrant/init config)]
    (try
      (f)
      (finally
        (integrant/halt! system)))))

(defn cleanup-todos [conn]
  (let [tx (->> (db/get-todos (d/db conn))
                (map (fn [todo]
                       [:db.fn/retractEntity [:todo/id (:todo/id todo)]])))]
    (d/transact conn tx)))

(defn cleanup-db [f]
  (let [conn (:democrado.db/conn system)]
    (try
      (f)
      (finally
        (cleanup-todos conn)))))
