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
            [audiobooks-creator-app.screens.recording.model :as m]))

(defn start-recording []
  (dispatch [::m/recording true])
  (-> nm/audio-recorder .startRecording)
  ;; -160dB 0dB
  (-> nm/audio-recorder
      (aset "onProgress"
            #(let [from 38 to 0]
               (dispatch [::m/monitoring
                          (-> % .-currentMetering
                              (+ from) (/ from) (* 100) (max 0))])))))

(defn stop-recording []
  (dispatch [::m/recording false])
  (-> nm/audio-recorder .stopRecording))

(defn monitor-line []
  (let [monitor-value (subscribe [::m/monitoring])
        in-progress?  (subscribe [::m/recording])
        top-w         (atom 0)
        line-bg       (fn [o]
                        [view {:style [st/box st/row (st/opacity o) (st/width @top-w)]}
                         [view {:style [(st/width "85%") (st/background "green")]}]
                         [view {:style [(st/width "10%") (st/background "yellow")]}]
                         [view {:style [(st/width "5%") (st/background "red")]}]])]
    (fn []
      [view {:on-layout (fn [e] (let [w (-> e .-nativeEvent .-layout .-width)]
                                  (println "on-layout:" e w) (reset! top-w w)))
             :style     [(st/gray 1)]}
       [line-bg 0.1]
       (if @in-progress?
         [view {:style [(st/height 6)
                        (st/margin 4 0)
                        (st/width (str @monitor-value "%"))
                        (st/overflow "hidden")]}
          [line-bg 1]]
         [view {:style [(st/height 14)]}])])))

(defn monitor []
  (let [monitor-value (subscribe [::m/monitoring])
        in-progress?  (subscribe [::m/recording])]
    (fn []
      [view {:style [(st/padding 0 0) (st/background "#aaa")]}
       [monitor-line]
       [spacer 4]
       [monitor-line]
       #_[view [text @monitor-value]]])))

(defn recording-button [icon-name focused on-press]
  (let [d (- 60 (* 2 8))]
    [rn/touchable-opacity {:style    [(st/margin 8)
                                      (st/flex)
                                      (st/background "white")
                                      (st/justify-content "center")
                                      (st/align-items "center")
                                      (st/rounded 4)
                                      (st/overflow "hidden")]
                           :on-press on-press}
     [view {:style {:justify-content "center" :align-items "center"}}
      [nm/icon-md {:color (if-not focused "#ccc" #_"#E6532C" "#FB783A") :size 38 :name icon-name}]]]))

(defn recording-controls []
  (let [in-progress? (subscribe [::m/recording])]
    (fn []
      [view {:style {:height 60 :flex-direction "row"}}
       (if @in-progress?
         [recording-button "stop" true #(stop-recording)]
         [recording-button "fiber-manual-record" false #(start-recording)])
       [recording-button "play-arrow" false #()]
       [recording-button "edit" false #()]])))

(comment
  (start-recording)
  (stop-recording))

