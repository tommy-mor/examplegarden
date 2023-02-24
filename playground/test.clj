(ns test
  (:require [bencode.core :as b]))

(require '[babashka.nrepl-client :as nrepl])


(defn connect
  [{:keys [host port expr]}]
  (let [s (java.net.Socket. (or host "localhost") (nrepl/coerce-long port))
        out (.getOutputStream s)
        in (java.io.PushbackInputStream. (.getInputStream s))
        id (nrepl/next-id)
        _ (b/write-bencode out {"op" "clone" "id" id})
        {session :new-session} (nrepl/read-msg (b/read-bencode in))
        id (nrepl/next-id)
        _ (b/write-bencode out {"op" "eval" "code" expr "id" id "session" session})]
    (loop [values []]
      (let [{:keys [status stdout value]} (nrepl/read-reply in session id)]
        (when stdout
          (print stdout)
          (flush))
        (if (= status ["done"])
          {:vals values :session session  :streams {:out out :in in}}
          (recur (cond-> values value (conj value))))))))

(def reply (connect {:port 1667 :expr "(+ 3 3)"}))

(def streams (:streams reply))
(def session (:session reply))

(let [id (nrepl/next-id)
      _ (b/write-bencode (:out streams) {"op" "time?" "id" id "session" session})]
  (nrepl/read-reply (:in streams) session id))

(defn test-function [a]
  (+ a 3))

(comment
  #record (test-function 8)
  (+ 5 5)
  (test-function 10))
