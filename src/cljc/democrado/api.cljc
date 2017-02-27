(ns democrado.api
  (:require
   [clojure.walk :refer [keywordize-keys]]))

(def todo-read-keys [:todo/id
                     :todo/description
                     :todo/completed
                     :todo/created-at])

(def todo-write-keys [:todo/description
                      :todo/completed])

(defn read-sanitize-todo [todo]
  (-> todo
      (select-keys todo-read-keys)))

(defn write-sanitize-todo [todo]
  (-> todo
      keywordize-keys ; for JSON
      (select-keys todo-write-keys)))
