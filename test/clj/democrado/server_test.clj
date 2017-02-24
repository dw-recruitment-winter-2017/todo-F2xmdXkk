(ns democrado.server-test
  (:require
   [byte-streams :as b]
   [clojure.test :as t]
   [clojure.test :refer [deftest is use-fixtures]]
   [cognitect.transit :as transit]
   [datomic.api :as d]
   [democrado.db :as db]
   [democrado.server :as server]
   [democrado.test-helper :as helper]
   [ring.mock.request :as ring.mock]
   [yada.yada :as yada])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(use-fixtures :once helper/init-system)
(use-fixtures :each helper/cleanup-db)

(def post-opts
  {:headers {"content-type" "application/json"}})

(def get-opts
  {:headers {"accept" "application/json"}})

(defn encode-transit [x]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)]
    (transit/write writer x)
    (.toString out)))

(defn decode-transit [s]
  (let [in (-> s .getBytes ByteArrayInputStream.)
        reader (transit/reader in :json)]
    (transit/read reader)))

(deftest test-add-todo []
  (let [conn (:democrado.db/conn helper/system)
        handler (yada/handler (server/notes-resource conn))
        todo {:todo/description "Test description"
              :todo/completed false}
        body (encode-transit todo)
        req (-> (ring.mock/request :post "/" body)
                (ring.mock/content-type "application/transit+json"))]
    (let [resp (handler req)
          db-todos (db/get-todos (d/db conn))
          db-todo (first db-todos)]
      (is (= 1 (count db-todos)))
      (is (= todo (select-keys db-todo [:todo/description
                                        :todo/completed]))))))

(deftest test-get-todos []
  (let [conn (:democrado.db/conn helper/system)
        todos [{:todo/description "Test description 1"
                :todo/completed false}
               {:todo/description "Test description 2"
                :todo/completed false}]]
    (doseq [todo todos]
      (db/add-todo! conn todo))
    (let [handler (yada/handler (server/notes-resource conn))
          req (-> (ring.mock/request :get "/")
                  (ring.mock/header "Accept" "application/transit+json"))
          resp (handler req)
          resp-todos (-> @(handler req)
                         :body
                         b/to-string
                         decode-transit)]
      (is (= 2 (count resp-todos)))
      (is (= todos (map #(select-keys % [:todo/description
                                         :todo/completed])
                        resp-todos))))))

(deftest test-put-todo []
  (let [conn (:democrado.db/conn helper/system)
        todo {:todo/description "Test description"
              :todo/completed false}
        todo-id (:todo/id (db/add-todo! conn todo))
        updated-todo {:todo/description "Updated test description"
                      :todo/completed true}
        handler (yada/handler (server/note-resource conn))
        body (encode-transit updated-todo)
        req (-> (ring.mock/request :put "/" body)
                (ring.mock/content-type "application/transit+json")
                (assoc :route-params {:id (str todo-id)}))]
    @(handler req)
    (let [db-todo (db/get-todo (d/db conn) todo-id)]
      (is (= updated-todo (select-keys db-todo [:todo/description
                                                :todo/completed]))))))
