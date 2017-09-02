(ns liguria.shared.native-modules
  (:require [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [reagent.core :as r :refer [atom]]
            [reagent.impl.component :as ru]))

(def modules (js/require "./src/js/modules.js"))
(def TabIcon (.-TabIcon modules))

(def icons (.-icons modules))

(def icon-fa (rn/adapt-react-class (.-IconFA icons) "icon-fa"))
(def icon-md (rn/adapt-react-class (.-IconMD icons) "icon-md"))
(def icon-io (rn/adapt-react-class (.-IconIO icons) "icon-io"))

(def audio-recorder (.-AudioRecorder modules))
(def audio-path (.-audioPath modules))

(def search-input (-> (js/require "react-native-search-input") .-SearchInput (rn/adapt-react-class "search-input")))

(def monitor-line (rn/adapt-react-class (.-Monitor modules) "Monitor"))

(comment
  (js/Object.keys modules))

