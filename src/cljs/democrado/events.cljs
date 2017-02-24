(ns democrado.events
  (:require
   [re-frame.core :as re-frame]
   [democrado.db :as db]
   [ajax.core :as ajax]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(defn set-page [db [_ page]]
  (assoc db :page page))

(re-frame/reg-event-db
 :set-page
 set-page)

(re-frame/reg-event-db
 :display-error
 (fn  [db _]
   (js/console.error "An error occurred communicating with the server")))

(re-frame/reg-event-fx
 :get-todos
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             "/api/notes"
                 :timeout         5000
                 :response-format (ajax/transit-response-format)
                 :on-success      [:load-todos]
                 :on-failure      [:display-error]}}))

(defn load-todos [db [_ todos]]
  (assoc db :todos-by-id
         (into {}
               (map (fn [todo] [(:todo/id todo) todo]))
               todos)))

(re-frame/reg-event-db
 :load-todos
 load-todos)

(defn update-new-todo [db [_ ks val]]
  (update db :new-todo #(assoc-in % ks val)))

(re-frame/reg-event-db
 :update-new-todo
 update-new-todo)

(defn add-todo [db [_ todo]]
  (let [todo-id (:todo/id todo)]
    (-> db
        (assoc :new-todo {})
        (assoc-in [:todos-by-id todo-id] todo))))

(re-frame/reg-event-db
 :add-todo
 add-todo)

(re-frame/reg-event-fx
 :post-todo
 (fn [{:keys [db]} [_ todo]]
   (let [todo (-> db
                  :new-todo
                  (assoc :todo/completed false))]
     {:http-xhrio {:method          :post
                   :uri             "/api/notes"
                   :params          todo
                   :timeout         5000
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [:add-todo]
                   :on-failure      [:display-error]}})))
