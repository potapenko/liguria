(ns audiobooks-creator-app.ios.core
  (:require [audiobooks-creator-app.app :refer [app-root-component]]
            [micro-rn.react-native :as c]
            [audiobooks-creator-app.events]
            [audiobooks-creator-app.subs]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-utils]))

(defn app-root []
  (println "App Loaded")
  (fn []
    [app-root-component]))

(defn init []
  (rn-utils/disable-overwriting-warnings)
  (dispatch-sync [:initialize-db])
  (.registerComponent c/AppRegistry "AudiobooksCreatorApp" #(r/reactify-component app-root)))
