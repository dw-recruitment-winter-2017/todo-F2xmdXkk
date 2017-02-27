(ns democrado.nav
  (:require
   #?@(:clj
       [[clojure.java.io :as io]
        [democrado.endpoints :as endpoints]
        [yada.yada :as yada]])
   #?@(:cljs
       [[bidi.bidi :as bidi]
        [pushy.core :as pushy]
        [re-frame.core :as re-frame :refer [dispatch subscribe]]])))

(defn mk-routes
  #?(:clj  [conn]
     :cljs [])
  [""
   [["/api/notes" [[""        #?(:clj  (endpoints/notes-resource conn)
                                 :cljs :notes-resource)]
                   [["/" :id] #?(:clj  (endpoints/note-resource conn)
                                 :cljs :note-resource)]]]
    ["/"          [[""        #?(:clj  (yada/as-resource (io/file "target/index.html"))
                                 :cljs :todo-list)]
                   ["about"   #?(:clj  (yada/as-resource (io/file "target/index.html"))
                                 :cljs :about)]
                   #?(:clj [""         (yada/as-resource (io/file "target"))])]]
    #?(:clj [true (yada/handler nil)])]])

#?(:cljs
   (do

     (defn set-page! [match]
       (dispatch [:set-page (:handler match)]))

     (defonce routes (mk-routes))

     (defonce history
       (pushy/pushy set-page! (partial bidi/match-route routes)))

     (defn start! []
       (pushy/start! history))

     ))
