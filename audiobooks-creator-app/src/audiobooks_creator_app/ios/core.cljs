(ns audiobooks-creator-app.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [audiobooks-creator-app.events]
            [audiobooks-creator-app.subs]
            [audiobooks-creator-app.app :refer [app-root-component]]
            [micro-rn.components :as c]))

(defn app-root []
  (println "App Loaded")
  (fn []
    [app-root-component]))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent c/AppRegistry "AudiobooksCreatorApp" #(r/reactify-component app-root)))
