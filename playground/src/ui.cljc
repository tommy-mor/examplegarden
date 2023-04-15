(ns ui
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]))

(e/def database (e/server (e/watch exprgarden.core/database)))

(e/defn Todo-list []
  (e/client
   (dom/div
    (dom/b (dom/text "examplegarden"))
    (e/server (e/for-by first [example database]
                        (e/client
                         (dom/pre (dom/text (contrib.str/pprint-str example)))
                         (ui/button (e/fn []
                                      (e/server
                                       (swap! exprgarden.core/database dissoc (first example))))
                                    (dom/text "delete"))))))))
