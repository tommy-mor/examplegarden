(ns test)

(require '[babashka.nrepl-client :as nrepl])

(nrepl/eval-expr {:port 1667 :expr "(+ 3 3)"})
