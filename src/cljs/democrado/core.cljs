(ns democrado.core
  (:require
   [day8.re-frame.http-fx]
   [democrado.config :as config]
   [democrado.events]
   [democrado.nav :as nav]
   [democrado.subs]
   [democrado.views :as views]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (nav/start!)
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  ;; TODO: is there a better way to do the initial loading?
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch-sync [:get-todos])
  (dev-setup)
  (mount-root))
