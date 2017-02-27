(ns democrado.views
  (:require
   [reagent.core :as reagent :refer [with-let]]
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [free-form.re-frame]))

(defn todo-list []
  [:table.table
   (into [:tbody]
         (for [{:keys [todo/id todo/description todo/completed]} @(subscribe [:todos])]
           [:tr
            [:td
             (if completed
               [:i.fa.fa-check-circle-o
                {:aria-hidden "true"
                 :on-click #(dispatch [:toggle-completed id])}]
               [:i.fa.fa-circle-o
                {:aria-hidden "true"
                 :on-click #(dispatch [:toggle-completed id])}])
             " "
             description
             [:i.fa.fa-trash.pull-right
              {:aria-hidden "true"
               :on-click #(dispatch [:delete-todo id])}]]]))])

;; TODO: can I get rid of preventDefault somehow?
(defn new-todo-form []
  [free-form.re-frame/form @(subscribe [:new-todo]) {} :update-new-todo
   [:form {:on-submit (fn [e]
                        (.preventDefault e)
                        (re-frame/dispatch [:add-todo]))}
    [:p
     [:input.form-control {:free-form/input {:key :todo/description}
                           :type            :text
                           :id              :todo-description
                           :placeholder     "To-do description"
                           :auto-complete   "off"}]]
    [:p
     [:button.btn.btn-primary.btn-block {:type "submit"}
      "Add"]]]])

(defn todo-page []
  [:div.row
   [:div.col-md-12
    [:h2 "Democrado"]
    [todo-list]
    [new-todo-form]]])

(defn about-page []
  [:div.row
   [:div.col-md-12
    [:h2 "About Democrado"]
    [:p "
Democrado is a small to-do list application implemented as part of my Democracy
Works job application. I admittedly went slightly overboard with it, but it
seemed like a good opportunity to experiment with some libraries I'd been
interested in for a while (Yada, bidi, and re-frame). If you're interested in
what I can do within a limited time, you can stop at
52ed2db5c960710289b94a48b81b9e428934caa2, that version implements all the basic
functionality, but is lacking some refinements."
     ]
    [:p "
To summarize how the application works, it uses Yada to serve RESTful HTTP
resources, bidi to route to those resources, and re-frame to interact with them
and render the client side user interface. Datomic is used as the backing data
store. Datomic is probably overkill for an application this simple, but the
assignment suggested the application should be built with extension in mind.
Given that requirement, I think it's a reasonable choice. Boot was chosen as a
build tool because the composability of its tasks (very useful for asset
pipelines)."
     ]]])

(defn main-panel []
  [:div.container
   (case @(subscribe [:page])
     :todo-list [todo-page]
     :about     [about-page]
     "Loading...")])
