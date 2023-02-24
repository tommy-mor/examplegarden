(ns exprgarden.core
  (:require [nrepl.misc :refer [response-for]  :as misc]
            [nrepl.middleware :as mw]
            [nrepl.transport :as t]
            [clojure.pprint]
            [duratom.core :as duratom]))

(comment "https://nrepl.org/nrepl/design/middleware.html"
         "https://github.com/gfredericks/debug-repl/blob/master/src/com/gfredericks/debug_repl.clj"
         "https://github.com/clojure-emacs/cider-nrepl/blob/1fa95f24d45af4181e5c1ce14bfa5fa97ff3a065/src/cider/nrepl/middleware/debug.clj")

(def database (duratom/duratom :local-file {:file-path ".exprgarden.edn"}))

(defn record-and-eval [msg form]
  (println "recording :3")
  (comment (spit "form.edn" form)
           (def form (read-string (slurp "playground/form.edn"))))
  
  (def nss (find-ns (symbol (or (:ns msg) "user"))))
  
  (comment (def nss (find-ns 'clojure.string))
           (.getName nss))

  (binding [*ns* nss]
    (swap! database assoc [(.getName nss) (first form)] (map eval (rest form)))
    (eval form)))

(defn maybe-record [h msg]
  (clojure.pprint/pprint (dissoc msg :transport :nrepl.middleware.print/print-fn))
  (let [is-record? (atom false)
        form
        (binding [*ns* (find-ns (symbol (or (:ns msg) "user")))
                  *data-readers* (assoc *data-readers* 'record
                                        (fn [x] (reset! is-record? true) x))]
          (read-string {:read-cond :allow}
                       (:code msg)))]
    (if @is-record?
      (do
        (println ":333")
        (t/send (:transport msg)
                (response-for msg :status :done :value
                              (pr-str (record-and-eval msg form))) ))
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

