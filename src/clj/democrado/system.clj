(ns democrado.system
  (:require
   [democrado.db]
   [democrado.server]
   [environ.core :refer [env]]
   [integrant.core :as integrant]))

(def config
  {:democrado.db/conn {:uri (or (env :democrado-db-uri)
                                "datomic:mem://democrado")}
   :democrado.server/listener {:conn (integrant/ref :democrado.db/conn)
                               :port (or (env :democrado-port)
                                         3000)}})
