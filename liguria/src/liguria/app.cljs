(ns liguria.app
  (:require [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [liguria.screens.recording.views :as recording]
            [liguria.screens.top.views :as top]
            [liguria.screens.wiki.views :as wiki]
            [liguria.screens.results.views :as results]
            [liguria.screens.lessons.views :as lessons]
            [liguria.screens.lessons.info :as info]
            [liguria.screens.bookmarks.views :as bookmarks]
            [micro-rn.utils :as utils]
            [liguria.shared.screens-shared-ui :as sh]
            [micro-rn.styles :as st]
            [micro-rn.rn-utils :as rn-utils]))

(def wiki-stack
  (nav/create-stack-navigator
   {:wiki {:screen wiki/main}}))

(def results-stack
  (nav/create-stack-navigator
   {:results {:screen results/main}}))

(def top-stack
  (nav/create-stack-navigator
   {:top {:screen top/main}}))

(defn build-header-right [component]
  (fn [props]
    (utils/prepare-to-js
     {:header-right
      (r/as-element [component (nav/props->navigator props)])})))

(def recording-stack
  (nav/create-stack-navigator
   {:lessons      {:screen lessons/main}
    :bookmarks    {:screen bookmarks/main}
    :lessons-info {:screen info/main}
    :recording    {:screen recording/main}}
   {:navigation-options (build-header-right lessons/leasons-right-header)}))

(def main-tabs
  (nav/create-tab-navigator
   {:recording-tab {:screen recording-stack}
    :results-tab {:screen results-stack}
    :top-tab {:screen top-stack}
    :wiki-tab {:screen wiki-stack}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])

