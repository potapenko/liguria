(ns user
  (:require [mount.core :as mount]
            podcaster-io-server.core))

(defn start []
  (mount/start-without #'podcaster-io-server.core/http-server
                       #'podcaster-io-server.core/repl-server))

(defn stop []
  (mount/stop-except #'podcaster-io-server.core/http-server
                     #'podcaster-io-server.core/repl-server))

(defn restart []
  (stop)
  (start))


