(ns exprgarden.db
  (:require [duratom.core :as duratom]
            [clojure.java.io :as jio]
            [edamame.core :as ed :refer [parse-string]]
            [zprint.core :as zp])
  
  (:import (clojure.lang IAtom IDeref)))

(defn read-file [fname]
  (let [f (jio/file fname)]
    (when-not (.exists f)
      (spit f "{}"))
    (ed/parse-string (slurp f) {:all true})))


(deftype EdnStore [local-file]

  IAtom
  (swap [_ assc coords values]
    (assert (= assc assoc))
    
    (spit local-file (zp/zprint-str (assoc (read-file local-file) coords values))))

  IDeref
  (deref [_]

    (read-file local-file)))

(comment
  (zp/zprint exprgarden.core/msg))

(comment
  (def x (new EdnStore ".test.edn"))
  (swap! x assoc 3 4)
  (-> x deref))

(defn ednstore [{:keys [file-path]}]
  (duratom/duratom :local-file {:file-path file-path}))



