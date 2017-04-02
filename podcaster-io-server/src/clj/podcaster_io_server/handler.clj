(ns podcaster-io-server.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [podcaster-io-server.layout :refer [error-page]]
            [podcaster-io-server.routes.home :refer [home-routes]]
            [podcaster-io-server.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [podcaster-io-server.env :refer [defaults]]
            [mount.core :as mount]
            [podcaster-io-server.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    #'service-routes
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
