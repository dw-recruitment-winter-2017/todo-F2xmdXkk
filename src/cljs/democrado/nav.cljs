(ns democrado.nav
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]))

(defn set-page! [match]
  (dispatch [:set-page (:handler match)]))

(def routes
  ["/" [["about" :about]
        [""      :todo-list]]])

(defonce history
  (pushy/pushy set-page! (partial bidi/match-route routes)))

(defn start! []
  (pushy/start! history))
