(ns audiobooks-creator-app.screens.recording.recorder
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view spacer flexer]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [audiobooks-creator-app.shared.screens-shared-ui :as sh]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]))

(defn monitor []
  (let [monitor-value (subscribe [::model/monitoring])
        recording?    (subscribe [::model/recording])]
    (fn []
      [view {:style {:padding 8 :flex-direction "row" :background-color "#aaa"}}
       [view
        [text "Monitoring:"]]
       [spacer 8]
       [view {:style {:padding 8 :flex 1 :flex-direction "row" :height 44}}
        [view {:style {:width (str @monitor-value "%") :background-color "red"}}]]])))

(defn recording-button [icon-name focused]
  (let [d (- 60 (* 2 8))]
    [rn/touchable-opacity {:style [(st/margin 8)
                                   (st/height d)
                                   (st/width (* 2 d))
                                   (st/background "white")
                                   (st/justify-content "center")
                                   (st/align-items "center")
                                   (st/rounded 4)
                                   (st/overflow "hidden")]}
     [view {:style {:justify-content "center" :align-items "center"}}
      [nm/icon-md {:color "#ccc" :size 38 :name icon-name}]]]))

(defn recording-controls []
  (let [recording?    (subscribe [::model/recording])]
    (fn []
      [view {:style {:flex 1 :flex-direction "row"}}
       [recording-button "fiber-manual-record" false]
       [flexer]
       [recording-button "edit" false]
       [flexer]
       [recording-button (if @recording? "stop" "play-arrow") false]])))

(defn start-recording [])

(defn stop-recording [])

(comment
  (start-recording)
  (stop-recording))

