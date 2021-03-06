(ns liguria.screens.results.views
  (:require [liguria.shared.native-modules :as nm]
            [re-frame.core :refer [dispatch subscribe]]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as utils]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.results.results-list :refer [results-list]]))

(defn icon-results [{:keys [tint-color focused] :as props}]
  [sh/tab-icon tint-color focused "ios-stats-outline"])

(defn- screen-content []
  (fn []
    [view {:style {:flex 1}}
     [results-list]]))

(def main
  (nav/create-screen
   {:title "Результаты"
    :tab-bar-icon #(r/as-element [icon-results (utils/prepare-to-clj %)])}
   (screen-content)))



