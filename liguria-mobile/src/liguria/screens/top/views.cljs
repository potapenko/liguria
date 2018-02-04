(ns liguria.screens.top.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as utils]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.top.top-list :refer [top-list]]
            [re-frame.core :refer [dispatch subscribe]]))

(defn icon-top [{:keys [tint-color focused] :as props}]
  [sh/tab-icon tint-color focused "ios-star-outline"])

(defn- screen-content []
  (fn []
    [top-list]))

(def main
  (nav/create-screen
   {:title "Топ"
    :tab-bar-icon #(r/as-element [icon-top (utils/prepare-to-clj %)])}
   (screen-content)))

