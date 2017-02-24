(ns democrado.system
  (:require
   [democrado.db]
   [democrado.server]
   [integrant.core :as integrant]))

(def config
  {:democrado.db/conn {:uri "datomic:mem://democrado"}
   :democrado.server/listener {:conn (integrant/ref :democrado.db/conn)
                               :port 3000}})
