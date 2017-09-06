(ns liguria.screens.wiki.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.wiki.wiki-list :refer [wiki-list]]
            [re-frame.core :refer [dispatch subscribe]]))

(defn icon-wiki [{:keys [tint-color focused]}]
  [sh/tab-icon tint-color focused "ios-book-outline"])

(defn- screen-content []
  (fn []
    [wiki-list]))

(def main
  (nav/create-screen
   {:title "Вики"
    :tab-bar-icon #(r/as-element [icon-wiki (util/prepare-to-clj %)])}
   (screen-content)))


