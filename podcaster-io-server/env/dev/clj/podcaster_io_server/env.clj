(ns podcaster-io-server.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [podcaster-io-server.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[podcaster-io-server started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[podcaster-io-server has shut down successfully]=-"))
   :middleware wrap-dev})
