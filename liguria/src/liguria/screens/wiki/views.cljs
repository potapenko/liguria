(ns liguria.screens.wiki.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.wiki.wiki-list :refer [wiki-list]]
            [re-frame.core :refer [dispatch subscribe]]
            [liguria.screens.wiki.model :as model]))

(defn build-test-data []
  (->> (range 1 10)
       (map #(do {:id   %
                  :date (str "10-12-2017 10:" %)
                  :text "hello"}))))

(defn- screen-content []
  (dispatch [::model/wiki-list (build-test-data)])
  (fn []
    [wiki-list]))

(def main
  (nav/create-screen
   {:title "Вики"
    :tab-bar-icon #(r/as-element [sh/icon-wiki (util/prepare-to-clj %)])}
   (screen-content)))


