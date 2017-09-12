(ns liguria.screens.lessons.views
  (:require [liguria.shared.native-modules :as nm]
            [re-frame.core :refer [dispatch subscribe]]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as utils]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.lessons.lessons-list :refer [lessons-list]]
            [micro-rn.utils :as utils]
            [micro-rn.styles :as st]))

(defn leasons-right-header [navigator]
  [rn/view {:style [st/row]}
   [rn/touchable-opacity
    {:style    [(st/padding 8)]
     :on-press #(nav/navigate! navigator :bookmarks)}
    [view {:style [st/row st/align-center st/justify-center]}
     [sh/icon-bookmark]]]
   [rn/touchable-opacity
    {:style    [(st/padding 8)]
     :on-press #(nav/navigate! navigator :lessons-info)}
    [view {:style [st/row st/align-center st/justify-center]}
     [sh/icon-info]]]
   [rn/spacer 20]])

(defn- screen-content []
  (fn [props]
    [view {:style {:flex 1}}
     [lessons-list (:navigation props)]]))

(def main
  (nav/create-screen
   {:title "Уроки"
    :tab-bar-icon #(r/as-element [sh/icon-lessons (utils/prepare-to-clj %)])}
   (screen-content)))

