(ns liguria.screens.settings.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]))

(defn- screen-content []
  (fn []
    [view {:style {:flex            1
                   :justify-content "center"
                   :align-items     "center"}}
     [text "Content"]]))

(def main
  (nav/create-screen
   {:title "Settings"
    :tab-bar-icon #(r/as-element [sh/icon-settings (util/prepare-to-clj %)])}
   (screen-content)))


