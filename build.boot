(set-env!
 :source-paths #{"src/clj" "src/cljc" "src/cljs" "test/clj" "test/cljs"}
 :resource-paths #{"assets"}
 :dependencies
 '[;; Dev
   [adzerk/boot-cljs "1.7.228-2"]
   [adzerk/boot-cljs-repl "0.3.3"]
   [adzerk/boot-reload "0.5.1"]
   [adzerk/boot-test "1.2.0"]
   [crisptrutski/boot-cljs-test "0.3.0"]
   [integrant/repl "0.1.0"]
   [org.clojure/tools.namespace "0.2.11"]
   [ring/ring-mock "0.3.0"]
   [samestep/boot-refresh "0.1.0"]

   ;; App
   [com.datomic/datomic-free "0.9.5561" :exclusions [io.netty/netty-all]]
   [com.pupeno/free-form "0.5.0"]
   [com.taoensso/timbre "4.8.0"]
   [day8.re-frame/http-fx "0.1.3"]
   [integrant "0.2.2"]
   [kibu/pushy "0.3.6"]
   [org.clojure/clojurescript "1.9.293"]
   [re-frame "0.9.2"]
   [reagent "0.6.0"]
   [yada "1.2.1"]])

(require '[datomic.api])
(load-data-readers!)

(require
 '[adzerk.boot-cljs :refer :all]
 '[adzerk.boot-reload :refer :all]
 '[adzerk.boot-test :refer :all]
 '[crisptrutski.boot-cljs-test :refer [test-cljs]]
 '[democrado.core]
 '[democrado.system]
 '[integrant.repl]
 '[samestep.boot-refresh :refer [refresh]])

(apply clojure.tools.namespace.repl/set-refresh-dirs (get-env :directories))

(deftask dev
  "Run dev environment."
  []
  (integrant.repl/set-prep! #(-> 'democrado.system/config resolve deref))
  (integrant.repl/go)
  (comp (repl)
        (watch)
        (refresh)
        (notify :audible false :visual true)
        (reload :on-jsload 'democrado.core/mount-root)
        (cljs :ids ["js/main"]
              :optimizations :none
              :compiler-options {:parallel-build true})
        (target)))
