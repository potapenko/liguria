(ns audiobooks-creator-app.android.core
  (:require [audiobooks-creator-app.app :refer [app-root-component]]
            audiobooks-creator-app.events
            audiobooks-creator-app.subs
            [micro-rn.react-native :as c]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r :refer [atom]]))

(defn app-root []
  (println "App Loaded")
  (fn []
    [app-root-component]))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent c/AppRegistry "AudiobooksCreatorApp" #(r/reactify-component app-root)))
