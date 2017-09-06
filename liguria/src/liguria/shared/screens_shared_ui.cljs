(ns liguria.shared.screens-shared-ui
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [micro-rn.styles :as st]))

(defn tab-icon [tint-color focused icon-name]
  [view {:style {:justify-content "center"
                 :align-items     "center"}}
   [nm/icon-io {:color tint-color :size 22 :name icon-name}]])

(defn icon-settings [{:keys [tint-color focused] :as props}]
  [tab-icon tint-color focused "ios-more-outline"])

(defn play-icon-style [color]
  [st/align-center st/justify-center
   (st/padding-left 3)
   (st/width 32)
   (st/height 32)
   (st/overflow "hidden")
   (st/rounded 20)
   (st/border 1 color)])

(defn play-icon []
  (let [color "cornflowerblue"]
    [view {:style (play-icon-style color)}
     [tab-icon color false "ios-play"]]))

(defn pause-icon []
  (let [color "cornflowerblue"]
    [view {:style (play-icon-style color)}
     [tab-icon color false "ios-play"]]))
