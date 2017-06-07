(ns audiobooks-creator-app.screens.recording.recorder
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as c :refer [alert text view spacer flexer]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [audiobooks-creator-app.shared.screens-shared-ui :as sh]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]))

(defn monitor []
  (let [monitor-value (subscribe [::model/monitoring])]
    (fn []
      [view {:style {:padding 8 :flex 1 :flex-direction "row"}}
       [view
        [view
         [text "Monitoring:"]
         [text "(" "paused" ")" @monitor-value]]]
       [spacer 8]
       [view {:style {:padding 8 :flex 1 :flex-direction "row"}}
        [view {:style {:width (str @monitor-value "%") :background-color "red"}}]]])))

(defn recording-controls [])

(defn start-recording [])

(defn stop-recording [])

(comment
  (start-recording)
  (stop-recording))

