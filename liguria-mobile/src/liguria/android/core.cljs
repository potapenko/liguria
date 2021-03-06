(ns liguria.android.core
  (:require [liguria.app :refer [app-root-component]]
            [micro-rn.react-native :as c]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r :refer [atom]]))

(defn app-root []
  (println "App Loaded")
  (fn []
    [app-root-component]))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent c/AppRegistry "Liguria" #(r/reactify-component app-root)))
