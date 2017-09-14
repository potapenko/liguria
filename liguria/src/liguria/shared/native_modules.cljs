(ns liguria.shared.native-modules
  (:require [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [reagent.core :as r :refer [atom]]
            [reagent.impl.component :as ru]
            [micro-rn.utils :as utils]))

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

(def update-scope (rn/adapt-react-class (.-UpdateScope modules) "UpdateScope"))

(def animatable-view (rn/adapt-react-class (.-AnimatableView modules) "AnimatableView"))
(def animatable-text (rn/adapt-react-class (.-AnimatableText modules) "AnimatableText"))

(defn animatable-new-animation [anims]
  (-> (.-Animatable modules)
      (.initializeRegistryWithDefinitions (utils/prepare-to-js anims))))

(def device-info (->> (js/require "react-native-device-info")
                     utils/prepare-to-clj
                     (map (fn [[k v]] {(-> k name (string/replace "get-" "") keyword) (v)}))
                     (apply merge)))

(comment
  (println (-> device-info))
  (js/Object.keys modules))

