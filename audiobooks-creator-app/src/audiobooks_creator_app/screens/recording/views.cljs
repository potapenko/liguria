(ns audiobooks-creator-app.screens.recording.views
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as c :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [audiobooks-creator-app.shared.screens-shared-ui :as sh]
            [audiobooks-creator-app.screens.recording.recorder :as recorder]
            [audiobooks-creator-app.screens.recording.model :as model]))

(defn- screen-content []
  (fn []
    [view {:style {:flex 1}}

     [view {:style {:flex-direction   "row"
                    :background-color "#ccc"
                    :height           60}}
      [recorder/monitor]]

     [view {:style {:flex 1}}
      [text "Recording"]]

     [view {:style {:flex-direction   "row"
                    :background-color "#ccc"
                    :height           60}}]]))

(def main
  (nav/create-screen
   {:title "Recording"
    :tab-bar-icon #(r/as-element [sh/icon-recording (util/prepare-to-clj %)])}
   (screen-content)))
