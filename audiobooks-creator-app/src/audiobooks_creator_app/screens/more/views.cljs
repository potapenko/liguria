(ns audiobooks-creator-app.screens.more.views
  (:require [audiobooks-creator-app.installed-components :as ic]
            [audiobooks-creator-app.native-modules :as nm]
            [micro-rn.react-native :as c :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [audiobooks-creator-app.screens-shared-ui :as sh]))

(defn- screen-content []
  (fn []
    [view {:style {:flex            1
                   :justify-content "center"
                   :align-items     "center"}}
     [text "Content"]]))

(def main
  (nav/create-screen
   {:title "More"
    :tab-bar-icon #(r/as-element [sh/icon-more (util/prepare-to-clj %)])}
   (screen-content)))
