(ns web)

(def start-electric-server! (delay @(requiring-resolve 'electric-server-java11-jetty10/start-server!)))
(def shadow-start! (delay @(requiring-resolve 'shadow.cljs.devtools.server/start!)))
(def shadow-watch (delay @(requiring-resolve 'shadow.cljs.devtools.api/watch)))


(def electric-server-config
  {:host "0.0.0.0", :port 8080, :resources-path "public"})




(defn main []
  
  (@shadow-start!)
  (@shadow-watch :dev)
  
  (def server (@start-electric-server! electric-server-config)))

