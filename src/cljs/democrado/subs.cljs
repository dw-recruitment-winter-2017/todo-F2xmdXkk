(ns democrado.subs
  (:require
   [re-frame.core :as re-frame]
   [taoensso.timbre :as log])
  (:require-macros
   [reagent.ratom :refer [reaction]]))

(re-frame/reg-sub
 :page
 (fn [db]
   (:page db)))

(re-frame/reg-sub
 :todos
 (fn [db]
   (->> (:todos-by-id db)
        (map second)
        (sort-by :todo/created-at)
        (into []))))

(re-frame/reg-sub
 :new-todo
 (fn [db]
   (:new-todo db)))
