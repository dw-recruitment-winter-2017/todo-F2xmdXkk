(ns democrado.server
  (:require
   [democrado.nav :as nav]
   [integrant.core :as integrant]
   [yada.yada :as yada]))

(defmethod integrant/init-key :democrado.server/listener [_ {:keys [conn port] :as opts}]
  (yada/listener
   (nav/mk-routes conn)
   {:port port}))

(defmethod integrant/halt-key! :democrado.server/listener [_ listener]
  ((:close listener)))
