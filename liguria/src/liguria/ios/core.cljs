(ns liguria.ios.core
  (:require #_[liguria.app :refer [app-root-component]]
            [micro-rn.react-native :as c]
            ;; [liguria.events]
            ;; [liguria.subs]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [reagent.core :as r :refer [atom]]
            ))

(defn app-root []
  (println "App Loaded")
  (fn []
    [c/view [c/text "hello"]]
    #_[app-root-component]))

(defn init []
  ;; (rn-utils/disable-overwriting-warnings)
  ;; (dispatch-sync [:initialize-db])
  (.registerComponent c/AppRegistry "Liguria" #(r/reactify-component app-root)))
