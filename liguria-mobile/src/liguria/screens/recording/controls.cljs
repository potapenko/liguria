(ns liguria.screens.recording.controls
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view spacer flexer]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [reagent.ratom :as ra]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [liguria.shared.screens-shared-ui :as sh]
            [re-frame.core :refer [subscribe dispatch]]
            [liguria.screens.recording.model :as model]
            [micro-rn.utils :as utils]
            [micro-rn.rn-utils :as rn-utils])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(def monitor-lines-ref (atom {}))
(def subscription (atom nil))

(def test-state (atom nil))

(defn start-recording []
  (dispatch [::model/recording true])
  (do
    (-> nm/speech-to-text (.startRecognition "ru-RU"))
    (reset! subscription
            (-> rn/NativeAppEventEmitter
                (.addListener "SpeechToText"
                              (fn [result]
                                (let [result (utils/prepare-to-clj result)]
                                  (println "speech to text: >>> " (-> result :best-transcription :formatted-string))
                                  (reset! test-state result)
                                  (-> @test-state :transcriptions),
                                  )
                                )))))
  (do
    (-> nm/audio-recorder .startRecording)
    ;; -160dB 0dB
    (-> nm/audio-recorder
        (aset "onProgress"
              #(let [from       38 to -5
                     monitoring (-> % .-currentMetering
                                    (+ from) (/ from) (* 100) (max 0))
                     set-width  (some-> @monitor-lines-ref .-setWidth)]
                 (when set-width
                   (set-width monitoring)))))))

(defn stop-recording []
  (dispatch [::model/recording false])
  (-> nm/speech-to-text .finishRecognition)
  (some-> @subscription .remove)
  (-> nm/audio-recorder .stopRecording))

(defn toggle-play []
  (if (= @(subscribe [::model/mode]) :playing)
    (dispatch [::model/mode :idle])
    (dispatch [::model/mode :playing])))

(defn monitor []
  (let [in-progress?  (subscribe [::model/recording])]
    (fn []
      [nm/monitor-line {:in-progress @in-progress? :ref #(reset! monitor-lines-ref %) }])))

(defn recording-button [icon-name focused on-press]
  (let [d (- 60 (* 2 8))]
    [rn/touchable-opacity {:style    [(st/margin 8 4)
                                      (st/flex)
                                      (st/background "white")
                                      (st/justify-content "center")
                                      (st/align-items "center")
                                      (st/rounded 2)
                                      (st/overflow "hidden")]
                           :on-press on-press}
     [view {:style {:justify-content "center" :align-items "center"}}
      [nm/icon-md {:color (if-not focused "#ccc" #_"#E6532C" "#FB783A") :size 30 :name icon-name}]]]))

(defn one-result
  ([res label] (one-result res label "cornflowerblue"))
  ([res label color]
   (let [recording @(subscribe [::model/recording])]
     [view {:style [st/align-center (st/flex) st/align-center (st/overflow "hidden")]}
      [text {:style [(st/font-size 48) (st/margin-top -5)
                     (st/color (if recording (st/gray-cl 1) color))]} (str res)]
      [text {:style [(st/color (if recording (st/gray-cl 2) "dimgray" ))
                     (st/margin-top -5)]} (str label)]])))

(defn progress-monitor []
  [view {:style [st/row st/align-center (st/padding 0 0 8 0) (st/background-color "#E9E9EF")]}
   [spacer 8]
   [one-result (str (rand-int 20) "%") "прогресс"]
   [one-result (-> 100 rand-int (/ 100) str) "скорость"]
   [one-result (rand-int 200) "балл"]
   [spacer 8]])

(defn recording-controls []
  (let [in-progress? (subscribe [::model/recording])
        mode         (subscribe [::model/mode])
        search?      #(= @mode :search)
        input-ref    (atom nil)]
    (fn []
      [view

       (if (search?)
         [view {:style [(st/height 50) (st/gray 1)]}
          [flexer]
          [nm/search-input {:ref              #(do (reset! input-ref %) (some-> @input-ref .focus))
                            :background-color st/nil-color
                            :on-cancel        #(go
                                                 (<! (timeout 300))
                                                 (dispatch [::model/mode :idle]))
                            :on-focus         #(dispatch [::model/mode :search])
                            :on-delete        #(utils/lazy-call (fn [] (dispatch [::model/search-text ""])))
                            :on-change-text   #(utils/lazy-call (fn [] (dispatch [::model/search-text %])))}]
          [flexer]]
         [view {:style [(st/height 50) st/row (st/padding 0 4)]}
          (if @in-progress?
            [recording-button "stop" true #(stop-recording)]
            [recording-button "fiber-manual-record" false #(start-recording)])
          [recording-button "play-arrow" (= @mode :playing) toggle-play]])

       [progress-monitor]
       [view {:style [(st/gray 1) (st/width 1)]}]

       ])))

(comment
  (subscribe [::model/search-text])
  (start-recording)
  (stop-recording))

