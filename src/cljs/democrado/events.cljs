(ns democrado.events
  (:require
   [ajax.core :as ajax]
   [bidi.bidi :as bidi]
   [democrado.api :as api]
   [democrado.db :as db]
   [democrado.nav :as nav]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(defn set-page [db [_ page]]
  (assoc db :page page))

(re-frame/reg-event-db
 :set-page
 set-page)

;; TODO: show error in the UI
(re-frame/reg-event-db
 :display-error
 (fn  [db _]
   (js/console.error "An error occurred communicating with the server")))

(re-frame/reg-event-fx
 :get-todos
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             (bidi/path-for nav/routes :notes-resource)
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

(defn load-todo [db [_ todo]]
  (let [todo-id (:todo/id todo)]
    (-> db
        (assoc :new-todo {})
        (assoc-in [:todos-by-id todo-id] todo))))

(re-frame/reg-event-db
 :load-todo
 load-todo)

(re-frame/reg-event-fx
 :add-todo
 (fn [{:keys [db]} [_ todo]]
   (let [todo (-> (:new-todo db)
                  (assoc :todo/completed false)
                  api/write-sanitize-todo)]
     {:http-xhrio {:method          :post
                   :uri             (bidi/path-for nav/routes :notes-resource)
                   :params          todo
                   :timeout         5000
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [:load-todo]
                   :on-failure      [:display-error]}})))

(re-frame/reg-event-fx
 :update-todo
 (fn [{:keys [db]} [_ todo-id todo]]
   (let [todo (api/write-sanitize-todo todo)]
     {:http-xhrio {:method          :put
                   :uri             (bidi/path-for nav/routes :note-resource :id (str todo-id))
                   :params          todo
                   :timeout         5000
                   :format          (ajax/transit-request-format)
                   :response-format (ajax/transit-response-format)
                   :on-success      [:load-todo]
                   :on-failure      [:display-error]}})))

(re-frame/reg-event-fx
 :complete-todo
 (fn [{:keys [db]} [_ todo-id]]
   (let [todo (-> (get-in db [:todos-by-id todo-id])
                  (assoc :todo/completed true)
                  api/write-sanitize-todo)]
     {:dispatch [:update-todo todo-id todo]})))

(re-frame/reg-event-fx
 :uncomplete-todo
 (fn [{:keys [db]} [_ todo-id]]
   (let [todo (-> (get-in db [:todos-by-id todo-id])
                  (assoc :todo/completed false)
                  api/write-sanitize-todo)]
     {:dispatch [:update-todo todo-id todo]})))

(re-frame/reg-event-fx
 :delete-todo
 (fn [{:keys [db]} [_ todo-id todo]]
   {:http-xhrio {:method          :delete
                 :uri             (bidi/path-for nav/routes :note-resource :id (str todo-id))
                 :timeout         5000
                 :format          (ajax/transit-request-format)
                 :response-format (ajax/transit-response-format)
                 :on-success      [:remove-todo todo-id]
                 :on-failure      [:display-error]}}))

(defn remove-todo [db [_ todo-id _]]
  (update db :todos-by-id dissoc todo-id))

(re-frame/reg-event-db
 :remove-todo
 remove-todo)
