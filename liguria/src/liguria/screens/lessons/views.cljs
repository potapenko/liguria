(ns liguria.screens.lessons.views
  (:require [liguria.shared.native-modules :as nm]
            [re-frame.core :refer [dispatch subscribe]]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.lessons.lessons-list :refer [lessons-list]]))

(defn icon-lessons [{:keys [tint-color focused] :as props}]
  [sh/tab-icon tint-color focused "ios-mic-outline"])

(defn- screen-content []
  (fn [props]
    [view {:style {:flex 1}}
     [lessons-list (:navigation props)]]))

(def main
  (nav/create-screen
   {:title "Уроки"
    :tab-bar-icon #(r/as-element [icon-lessons (util/prepare-to-clj %)])}
   (screen-content)))



