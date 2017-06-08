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

(defn start-recording []
  (dispatch [::model/recording true])
  (-> nm/audio-recorder .startRecording)
  (-> nm/audio-recorder (aset "onProgress" #(dispatch [::model/monitoring (-> % .-currentMetering (+ 100))]))))

(defn stop-recording []
  (dispatch [::model/recording false])
  (-> nm/audio-recorder .stopRecording))

(defn monitor-line []
  (let [monitor-value (subscribe [::model/monitoring])
        in-progress?    (subscribe [::model/recording])
        bg (fn [o]
             [view {:style [st/box st/row (st/opacity o)]}
              [view {:style [(st/width "85%") (st/background "green")]}]
              [view {:style [(st/width "10%") (st/background "yellow")]}]
              [view {:style [(st/width "5%") (st/background "red")]}]])]
    (fn []
      [view {:style [(st/gray 1)]}
       [bg 0.4]
       (if @in-progress?
           [view {:style [(st/height 6)
                          (st/margin 4 0)
                          (st/width #_"10%" (str @monitor-value "%"))
                          (st/border)
                          (st/opacity 0.3)
                          (st/background "black")]}]
           [view {:style [(st/height 14)]}])])))

(defn monitor []
  (let [monitor-value (subscribe [::model/monitoring])
        in-progress?    (subscribe [::model/recording])]
    (fn []
      [view {:style {:padding 8 :background-color "#aaa"}}
       [monitor-line]
       [spacer 4]
       [monitor-line]])))

(defn recording-button [icon-name focused on-press]
  (let [d (- 60 (* 2 8))]
    [rn/touchable-opacity {:style [(st/margin 8)
                                   (st/flex)
                                   (st/background "white")
                                   (st/justify-content "center")
                                   (st/align-items "center")
                                   (st/rounded 4)
                                   (st/overflow "hidden")]
                           :on-press on-press}
     [view {:style {:justify-content "center" :align-items "center"}}
      [nm/icon-md {:color "#ccc" :size 38 :name icon-name}]]]))

(defn recording-controls []
  (let [in-progress?    (subscribe [::model/recording])]
    (fn []
      [view {:style {:height 60 :flex-direction "row"}}
       [recording-button
        (if @in-progress? "stop" "fiber-manual-record") false
        #(if @in-progress? (stop-recording) (start-recording))]
       [recording-button "play-arrow" false #()]
       [recording-button "edit" false #()]])))

(comment
  (start-recording)
  (stop-recording))

