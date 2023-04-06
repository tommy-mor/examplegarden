(ns exprgarden.db
  (:require [duratom.core :as duratom]))

(defn ednstore [{:keys [file-path]}]
  (duratom/duratom :local-file {:file-path file-path}))

