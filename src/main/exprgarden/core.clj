(ns exprgarden.core
  (:require [nrepl.misc :refer [response-for]  :as misc]
            [nrepl.middleware :as mw]
            [nrepl.transport :as t]
            [clojure.pprint]))

(comment "https://nrepl.org/nrepl/design/middleware.html"
         "https://github.com/gfredericks/debug-repl/blob/master/src/com/gfredericks/debug_repl.clj"
         "https://github.com/clojure-emacs/cider-nrepl/blob/1fa95f24d45af4181e5c1ce14bfa5fa97ff3a065/src/cider/nrepl/middleware/debug.clj")

(defn record-and-eval [form]
  (println "recording :3")
  (eval form))

(defn maybe-record [h msg]
  (clojure.pprint/pprint (dissoc msg :transport :nrepl.middleware.print/print-fn))
  (let [is-record? (atom false)]
    (binding [*ns* (find-ns (symbol (or (:ns msg) "user")))
              *data-readers* (assoc *data-readers* 'record
                                    (fn [x] (reset! is-record? true) x))]
      (read-string {:read-cond :allow}
                   (:code msg)))
    (if @is-record?
      (do
        (println ":333")
        (t/send (:transport msg) (response-for msg :status :done :value "8") ))
      (h msg))))

(defn current-time
  [h]
  (fn [{:keys [op transport] :as msg}]
    (case op
      "eval" (maybe-record h msg)


      
      (h msg))))

(mw/set-descriptor!
 #'current-time
 {:expects #{"eval" "clone"}})

