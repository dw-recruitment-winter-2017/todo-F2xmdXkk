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
                 :on-click #(dispatch [:uncomplete-todo id])}]
               [:i.fa.fa-circle-o
                {:aria-hidden "true"
                 :on-click #(dispatch [:complete-todo id])}])
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
    "Democrado is a take home assignment for my Democracy Works job application. In a future commit I will describe it in more detail here."]])

(defn main-panel []
  [:div.container
   (case @(subscribe [:page])
     :todo-list [todo-page]
     :about     [about-page]
     "Loading...")])
