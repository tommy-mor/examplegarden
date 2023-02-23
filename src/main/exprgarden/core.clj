(ns exprgarden.core
  (:require [nrepl.misc :refer [response-for]  :as misc]
            [nrepl.middleware :as mw]
            [nrepl.transport :as t]))

(comment "https://nrepl.org/nrepl/design/middleware.html"
         "https://github.com/gfredericks/debug-repl/blob/master/src/com/gfredericks/debug_repl.clj")

(defn current-time
  [h]
  (fn [{:keys [op transport] :as msg}]
    (println ":3")
    (if (= "time?" op)
      (t/send transport (response-for msg :status :done :time (System/currentTimeMillis)))
      (h msg))))

(mw/set-descriptor!
 #'current-time
 {:expects #{"eval" "clone"}})

