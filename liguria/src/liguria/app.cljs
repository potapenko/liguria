(ns liguria.app
  (:require [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [liguria.screens.recording.views :as recording]))

(def recording-stack
  (nav/create-stack-navigator
   {:books {:screen recording/main}}))

(def main-tabs
  (nav/create-tab-navigator
   {:recording-tab {:screen recording-stack}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])

