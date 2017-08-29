(ns liguria.shared.native-modules
  (:require [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [reagent.core :as r :refer [atom]]
            [reagent.impl.component :as ru]))

(def modules (js/require "./src/js/modules.js"))
(def TabIcon (.-TabIcon modules))

(def icons (.-icons modules))

(def icon-fa (r/adapt-react-class (.-IconFA icons)))
(def icon-md (r/adapt-react-class (.-IconMD icons)))
(def icon-io (r/adapt-react-class (.-IconIO icons)))

(def audio-recorder (.-AudioRecorder modules))
(def audio-path (.-audioPath modules))

(def search-input (-> (js/require "react-native-search-input") .-SearchInput r/adapt-react-class))

(comment
  (js/Object.keys modules))

