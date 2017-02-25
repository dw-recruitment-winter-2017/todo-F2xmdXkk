(ns democrado.events-test
  (:require
   [democrado.events :as events]
   [clojure.test :refer [deftest is]]))

(deftest test-load-todos
  (let [todo-id #uuid "58b086ff-dd16-4e2d-924f-f8a952b33b50"
        todo {:todo/id todo-id
              :todo/created-at #inst "2017-02-24T19:18:23.317-00:00"
              :todo/description "Test description"
              :todo/completed false}
        todos [todo]
        db {:todos-by-id {}}
        ret-db (events/load-todos db [nil todos])]
    (is (= ret-db {:todos-by-id {todo-id todo}}))))

(deftest test-update-new-todo
  (let [db {:new-todo {}}
        ret-db (events/update-new-todo db [nil [:todo/description] "Test"])]
    (is (= ret-db {:new-todo {:todo/description "Test"}}))))

(deftest test-load-todo
  (let [todo-id #uuid "58b086ff-dd16-4e2d-924f-f8a952b33b50"
        todo {:todo/id todo-id
              :todo/created-at #inst "2017-02-24T19:18:23.317-00:00"
              :todo/description "Test description"
              :todo/completed false}
        db {:new-todo todo :todos-by-id {}}
        ret-db (events/load-todo db [nil todo])]
    (is (= ret-db {:new-todo {}
                   :todos-by-id {todo-id todo}}))))

(deftest test-remove-todo
  (let [todo-id #uuid "58b086ff-dd16-4e2d-924f-f8a952b33b50"
        db {:todos-by-id
            {todo-id
             {:todo/id todo-id
              :todo/created-at #inst "2017-02-24T19:18:23.317-00:00"
              :todo/description "Test description"
              :todo/completed false}}}
        ret-db (events/remove-todo db [nil todo-id nil])]
    (is (= ret-db {:todos-by-id {}}))))
