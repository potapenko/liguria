(ns liguria.shared.screens-shared-ui
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]))

(defn tab-icon [tint-color focused icon-name]
  [view {:style {:justify-content "center"
                 :align-items     "center"}}
   [nm/icon-io {:color tint-color :size 22 :name icon-name}]])

(defn icon-recording [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-mic-outline"])

(defn icon-settings [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-more-outline"])

(defn icon-top [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-star-outline"])

(defn icon-results [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-albums-outline"])

(defn icon-wiki [{:keys [tint-color focused]}]
  [tab-icon tint-color focused "ios-book-outline"])
