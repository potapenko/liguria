(ns liguria.screens.top.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.top.top-list :refer [top-list]]
            [re-frame.core :refer [dispatch subscribe]]
            [liguria.screens.top.model :as model]))

(defn build-test-data []
  (->> (range 1 10)
       (map #(do {:id   %
                  :date (str "10-12-2017 10:" %)
                  :text "hello"}))))

(defn- screen-content []
  (dispatch [::model/top-list (build-test-data)])
  (fn []
    [top-list]))

(def main
  (nav/create-screen
   {:title "Топ"
    :tab-bar-icon #(r/as-element [sh/icon-top (util/prepare-to-clj %)])}
   (screen-content)))


