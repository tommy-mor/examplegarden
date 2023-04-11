(ns ui
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]))

(e/defn Todo-list []
  (e/client (dom/pre (dom/text "epic!!!"))))
