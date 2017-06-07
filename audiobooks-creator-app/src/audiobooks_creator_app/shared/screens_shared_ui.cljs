(ns audiobooks-creator-app.shared.screens-shared-ui
  (:require [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as c :refer [alert text view]]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]))

(defn tab-icon [tint-color focused icon-name]
  [view {:style {:justify-content "center"
                 :align-items     "center"}}
   [nm/icon-io {:color tint-color :size 22 :name icon-name}]])

(defn icon-books [{:keys [tint-color focused]}]
  [tab-icon tint-color focused "ios-book"])

(defn icon-more [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-more"])

(defn icon-bookshelf [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-bookmark"])

(defn icon-friends [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-people"])

(defn icon-recording [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-mic"])

(defn icon-settings [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-cog"])
