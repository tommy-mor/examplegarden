(ns exprgarden.core
  (:require [nrepl.misc :refer [response-for]  :as misc]
            [nrepl.middleware :as mw]
            [nrepl.transport :as t]
            [clojure.pprint]
            [cider.nrepl.middleware.info :as cider-info]
            [cider.nrepl.middleware.util.error-handling :refer [with-safe-transport]]
            [duratom.core :as duratom]
            [clojure.data.json :as json]))

(comment "https://nrepl.org/nrepl/design/middleware.html"
         "https://github.com/gfredericks/debug-repl/blob/master/src/com/gfredericks/debug_repl.clj"
         "https://github.com/clojure-emacs/cider-nrepl/blob/1fa95f24d45af4181e5c1ce14bfa5fa97ff3a065/src/cider/nrepl/middleware/debug.clj")

(comment "TODO"
         "ignore serializing some things, give warning"
         "find a way to mark functions deeper in the stack, not just toplevel #record")


(def database (duratom/duratom :local-file {:file-path ".exprgarden.edn"}))

(defn lookup-database [msg]
  (get @database [(symbol (:ns msg)) (symbol (:sym msg))]))

(defn record-and-eval [msg form]
  (def form form)
  (def nss (find-ns (symbol (or (:ns msg) "user"))))
  
  (comment (def nss (find-ns 'clojure.string))
           (.getName nss))

  "TODO use edamame edn parser/writer so that i can avoid #objects and other stuff like that"

  (binding [*ns* nss]
    (swap! database assoc [(.getName nss) (first form)] (map eval (rest form)))
    (eval form)))

(defn recall-and-eval [msg form]
  ;; ASSUMING THAT THERE IS NO DOCSTRING! ~= ASSUMING SIMPLEST DEFN FORM
  (assert (= (first form) 'defn))
  (def fname (second form))
  (def nss (symbol (:ns msg)))
  (def binding-values (-> database deref (get [nss fname])))
  (def arglist (nth form 2))
  (def bindings (->> (interleave arglist binding-values)
                     vec))
  (def body (drop 3 form))

  (binding [*ns* (create-ns nss)]
    (eval `(let ~bindings ~@body ))))

(defn has-tag [tag-symbol msg]
  (let [found? (atom false)
        form
        (binding [*ns* (find-ns (symbol (or (:ns msg) "user")))
                  *data-readers* (assoc *data-readers*
                                        'record (fn [x] x)
                                        'recall (fn [x] x)
                                        tag-symbol (fn [x] (reset! found? true) x))]
          (read-string {:read-cond :allow}
                       (:code msg)))]
    {:form form :has-tag? @found?}))

(defn maybe-record [h msg]
  (clojure.pprint/pprint (dissoc msg :transport :nrepl.middleware.print/print-fn))
  (let [{record? :has-tag?} (has-tag 'record msg)
        {recall? :has-tag? form :form} (has-tag 'recall msg)]
    
    (cond record?
          (t/send (:transport msg)
                  (response-for msg :status :done :value
                                (pr-str (record-and-eval msg form))) )

          recall?
          (t/send (:transport msg)
                  (response-for msg
                                :status :done
                                :value (pr-str (recall-and-eval msg form))))
          
          :else
          (h msg))))


(defn maybe-info [h msg]
  (println (:sym msg))
  
  (comment (update (cider-info/format-response (cider-info/info msg))))
  
  (if-let [values (lookup-database msg)]
    (t/send (:transport msg)
            (response-for msg (-> (cider-info/format-response (cider-info/info msg))
                                  (assoc "status" "done")
                                  (update "arglists-str"
                                          str "\nexpgarden values: " (lookup-database msg))
                                  (clojure.walk/keywordize-keys))))

    (h msg)))

(defn examplegarden-hook
  [h]
  (fn [{:keys [op transport] :as msg}]
    (println "op" op)
    (case op
      "eval" (maybe-record h msg)
      "info" (maybe-info h msg)


      
      (h msg))))


(defn fubar [a b c]
  (+ a b (+ a (* b b))))

(defn bar [epic]
  (clojure.string/join ", epic, " epic))

(defn process-catfact [raw]
  (json/read-str raw))

(comment
  (process-catfact (slurp "https://catfact.ninja/fact")))

(comment
  (fubar 4 2 1)

  (+ 3 3))

(mw/set-descriptor!
 #'examplegarden-hook
 {:expects #{"eval" "clone"}})

