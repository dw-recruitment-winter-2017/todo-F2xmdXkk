(ns democrado.core
  (:require
   [democrado.system :as system]
   [integrant.core :as integrant]))

(defn -main
  [& args]
  (let [system (integrant/init system/config)]
    (println "System started...")))
