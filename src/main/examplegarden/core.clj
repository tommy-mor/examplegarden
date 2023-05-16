(ns examplegarden.core
  (:require [examplegarden.db :as db]
            [nrepl.misc :refer [response-for]  :as misc]
            [nrepl.middleware :as mw]
            [nrepl.transport :as t]
            [clojure.pprint]
            
            [cider.nrepl.middleware.info :as cider-info]
            [cider.nrepl.middleware.util.error-handling :refer [with-safe-transport]]
            [clojure.data.json :as json]
            [clojure.walk :as walk]))

(comment "https://nrepl.org/nrepl/design/middleware.html"
         "https://github.com/gfredericks/debug-repl/blob/master/src/com/gfredericks/debug_repl.clj"
         "https://github.com/clojure-emacs/cider-nrepl/blob/1fa95f24d45af4181e5c1ce14bfa5fa97ff3a065/src/cider/nrepl/middleware/debug.clj")

(comment "add column tag to coordinates/ui"
         "parse function definitions using clojure spec"
         "M-N bind that changes form to include not entire body, but chops off the body of the function after given bind"

         "electric ui to chose threads, and vis data"
         "  - electric ui show examples, live update when it changes.."
         "  - electric ui buttons to switch between which thread to use"
         "  - electric ui send to openai/copilot"

         "make the store store types in transit, so you can extend the datatypes at the bottom")


(def database (db/ednstore {:file-path ".examplegarden.edn"}))

(defn lookup-database [msg]
  (get @database [(symbol (:ns msg)) (symbol (:sym msg))]))

(defn unserializable? [v]
  (cond (fn? v) true
        (string? v) false
        :else (try (do (read-string (pr-str v)) false)
                   (catch Exception e true))))

(defn force-serializable [v]
  (walk/postwalk (fn [v]
                   (cond
                     (coll? v) v
                     (unserializable? v) (do
                                           (println "dropping unserializable value: " v)
                                           nil)
                     :else v)) v))

(defn record-and-eval [msg form]
  (def msg msg)
  (def form form)
  (def nss (find-ns (symbol (or (:ns msg) "user"))))

  (binding [*ns* nss]
    (swap! database assoc [(.getName nss) (first form)] (force-serializable (map eval (rest form))))
    (eval form)))

(comment (record-and-eval msg form))

(defn recall-and-eval [msg form]
  (let [form (if (= (first form) 'exprgarden.core/record)
               (second form)
               form)]

    (def form form)
    
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
      (eval `(let ~bindings ~@body )))))

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
  (comment (clojure.pprint/pprint (dissoc msg :transport :nrepl.middleware.print/print-fn)))
  
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
  (comment (println (:sym msg)))
  
  (comment (update (cider-info/format-response (cider-info/info msg))))
  
  (if-let [values (lookup-database msg)]
    (t/send (:transport msg)
            (response-for msg (-> (cider-info/format-response (cider-info/info msg))
                                  (assoc "status" "done")
                                  (update "arglists-str"
                                          str "\nexpgarden values: " (pr-str (lookup-database msg)))
                                  (clojure.walk/keywordize-keys))))

    (h msg)))

(defn examplegarden-hook
  [h]
  (fn [{:keys [op transport] :as msg}]
    (comment (println "op" op))
    (case op
      "eval" (maybe-record h msg)
      "info" (maybe-info h msg)


      
      (h msg))))

(defn bug [nme ns argvals]
  (swap! database assoc [(.getName ns) nme] argvals))

(defmacro record [[dfn nme oldargs & body]]
  (assert (= dfn 'defn) )
  (let [newargs (vec (map (fn [_] (gensym)) oldargs))
        newlet (vec (interleave oldargs newargs))]
    `(defn ~nme ~newargs
       (bug '~nme ~*ns* ~newargs)
       (let ~newlet
         ~@body))))

(examplegarden.core/record (defn fubar [a b c]
                           (+ a b (+ a (* b b)))))

(defn testfn [a]
  (fubar (inc a) a a))

(defn bar [epic]
  (clojure.string/join ", " epic))

(examplegarden.core/record (defn process-catfact [raw]
                             (json/read-str raw :key-fn keyword)))

(defn process-catfact [raw]
  #_(json/read-str raw)
  (json/read-str raw :key-fn keyword))

(comment
  
  (process-catfact (slurp "https://catfact.ninja/fact"))
  (bar [1 2  3 4])

  (fubar 3 2 1)

  (+ 3 3))

(mw/set-descriptor!
 #'examplegarden-hook
 {:expects #{"eval" "clone"}})

