(ns democrado.db-test
  (:require
   [clojure.test :refer [deftest is use-fixtures]]
   [datomic.api :as d]
   [democrado.db :as db]
   [democrado.server]
   [democrado.test-helper :as helper]))

(use-fixtures :once helper/init-system)
(use-fixtures :each helper/cleanup-db)

(deftest test-add-todo []
  (let [conn (:democrado.db/conn helper/system)
        todo {:todo/description "Test description"
              :todo/completed false}]
    (db/add-todo! conn todo)
    (let [db-todos (db/get-todos (d/db conn))
          db-todo (first db-todos)]
      (is (= 1 (count db-todos)))
      (is (contains? db-todo :todo/id))
      (is (= todo (select-keys db-todo [:todo/description
                                        :todo/completed]))))))

(deftest test-get-todos []
  (let [conn (:democrado.db/conn helper/system)
        todos #{{:todo/description "Test description 1"
                 :todo/completed false}
                {:todo/description "Test description 2"
                 :todo/completed false}}]
    (doseq [todo todos]
      (db/add-todo! conn todo))
    (let [db-todos (db/get-todos (d/db conn))]
      (is (= 2 (count todos)))
      (is (= todos (into #{}
                         (map #(select-keys % [:todo/description
                                               :todo/completed]))
                         db-todos))))))

(deftest test-get-todo []
  (let [conn (:democrado.db/conn helper/system)
        todo {:todo/description "Test description"
              :todo/completed false}
        todo-id (:todo/id (db/add-todo! conn todo))
        db-todo (db/get-todo (d/db conn) todo-id)]
    (is (= todo (select-keys db-todo [:todo/description
                                      :todo/completed])))))

(deftest test-update-todo []
  (let [conn (:democrado.db/conn helper/system)
        todo {:todo/description "Test description"
              :todo/completed false}
        todo-id (:todo/id (db/add-todo! conn todo))
        updated-todo {:todo/description "Updated test description"
                      :todo/completed true}]
    (db/update-todo! conn todo-id updated-todo)
    (let [db-todo (db/get-todo (d/db conn) todo-id)]
      (is (= updated-todo (select-keys db-todo [:todo/description
                                                :todo/completed]))))))
