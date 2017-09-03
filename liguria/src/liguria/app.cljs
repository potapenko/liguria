(ns liguria.app
  (:require [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [liguria.screens.recording.views :as recording]
            [liguria.screens.top.views :as top]
            [liguria.screens.wiki.views :as wiki]
            [liguria.screens.results.views :as results]))

(def wiki-stack
  (nav/create-stack-navigator
   {:books {:screen wiki/main}}))

(def results-stack
  (nav/create-stack-navigator
   {:books {:screen results/main}}))

(def top-stack
  (nav/create-stack-navigator
   {:books {:screen top/main}}))

(def recording-stack
  (nav/create-stack-navigator
   {:books {:screen recording/main}}))

(def main-tabs
  (nav/create-tab-navigator
   {:results-tab {:screen results-stack}
    :recording-tab {:screen recording-stack}
    :top-tab {:screen top-stack}
    :wiki-tab {:screen wiki-stack}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])

