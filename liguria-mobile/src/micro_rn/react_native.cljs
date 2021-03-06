(ns micro-rn.react-native
  (:require
   [reagent.core :as r :refer [atom]]
   [reagent.impl.component :as ru]
   [micro-rn.styles :as s :refer [get-style new-style flex row column
                                  opacity background gray white red
                                  orange yellow olive green teal
                                  blue violet purple grey pink
                                  brown black rounded border height
                                  width align-center align-right padding
                                  margin padding-horizontal padding-vertical
                                  font-size bold color stretch
                                  position-absolute top left bottom right
                                  overflow shadow text-align text-shadow]]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros
   [reagent.ratom :refer [reaction]]
   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def ReactNative (js/require "react-native"))

(defn adapt-react-class
  ([cl] (adapt-react-class cl nil))
  ([cl message]
   (try
     (r/adapt-react-class cl)
     (catch js/Error e
       (let [m (str "Error load module(" (or message "unnamed") "): nil?" (nil? cl))]
         (println ">>> !! >>>" m)
         (throw (new js/Error m)))))))

(def device (.get ReactNative.Dimensions "window"))

(defmacro create-components [names]
  (let [create-def (fn [prop-name component-name]
                     `(def ~(symbol prop-name)
                        (when-let [react-component (aget ReactNative ~component-name)]
                          (reagent.core/adapt-react-class react-component))))]
    `(do ~@(map create-def names))))

; build-in сomponents

(def image (adapt-react-class (.-Image ReactNative) "image"))
(def list-view (adapt-react-class (.-ListView ReactNative) "list-view"))
(def flat-list (adapt-react-class (.-FlatList ReactNative) "flat-list"))
(def virtualized-list (adapt-react-class (.-VirtualizedList ReactNative) "virtualized-list"))
(def section-list (adapt-react-class (.-SectionList ReactNative) "section-list"))
(def refresh-control (adapt-react-class (.-RefreshControl ReactNative) "refresh-control"))
;; (def map-view (adapt-react-class (.-MapView ReactNative) "map-view"))
(def modal (adapt-react-class (.-Modal ReactNative) "modal"))
(def refresh-contol (adapt-react-class (.-RefreshControl ReactNative) "refresh-contol"))
(def scroll-view (adapt-react-class (.-ScrollView ReactNative) "scroll-view"))
(def switch (adapt-react-class (.-Switch ReactNative) "switch"))
(def text (adapt-react-class (.-Text ReactNative) "text"))
(def text-input (adapt-react-class (.-TextInput ReactNative) "text-input"))
(def touchable-without-feedback (adapt-react-class (.-TouchableWithoutFeedback ReactNative) "touchable-without-feedback"))
(def touchable-highlight (adapt-react-class (.-TouchableHighlight ReactNative) "touchable-highlight"))
(def touchable-native-feedback (adapt-react-class (.-TouchableNativeFeedback ReactNative) "touchable-native-feedback"))
(def touchable-opacity (adapt-react-class (.-TouchableOpacity ReactNative) "touchable-opacity"))
(def view (adapt-react-class (.-View ReactNative) "view"))
(def animated-view (adapt-react-class (.-View (.-Animated ReactNative)) "animated-view"))
(def web-view (adapt-react-class (.-WebView ReactNative) "web-view"))
(def activity-indicator (adapt-react-class (.-ActivityIndicator ReactNative) "activity-indicator"))
(def slider (adapt-react-class (.-Slider ReactNative) "slider"))
(def picker (adapt-react-class (.-Picker ReactNative) "picker"))
(def picker-item (adapt-react-class (.-Picker.Item ReactNative) "picker-item"))
(def status-bar (adapt-react-class (.-StatusBar ReactNative) "status-bar"))

; apis

(defn alert
  ([message] (.alert (.-Alert ReactNative) "Alert" message))
  ([title message] (.alert (.-Alert ReactNative) title message nil))
  ([title message buttons]
   (.alert (.-Alert ReactNative) title message (utils/prepare-to-js buttons))))

(def AppRegistry (.-AppRegistry ReactNative))
(def AppState (.-AppState ReactNative))
(def AsyncStorage (.-AsyncStorage ReactNative))
(def CameraRoll (.-CameraRoll ReactNative))
(def dimensions (.-Dimensions ReactNative))
(def InteractionManager (.-InteractionManager ReactNative))
(def NativeModules (.-NativeModules ReactNative))
(def NativeAppEventEmitter (.-NativeAppEventEmitter ReactNative))
(def DeviceEventEmitter (.-DeviceEventEmitter ReactNative))

(def Platform (utils/prepare-to-clj (.-Platform ReactNative)))
(def android? (= "android" (:os Platform)))
(def ios? (= "ios" (:os Platform)))

(def layout-animation (.-LayoutAnimation ReactNative))
(def layout-animation-configure-next (.-configureNext layout-animation))
(def layout-animation-presets (.-Presets layout-animation))
(def layout-animation-presets-spring (.-spring layout-animation-presets))
(def layout-animation-presets-linear (.-linear layout-animation-presets))
(def layout-animation-presets-ease-in-ease-out (.-easeInEaseOut layout-animation-presets))

(defn animate-layout
  ([] (animate-layout "spring"))
  ([anim-type] (let [type (case anim-type
                            "spring" layout-animation-presets-spring
                            "linear" layout-animation-presets-linear
                            "ease-in-ease-out" layout-animation-presets-ease-in-ease-out
                            layout-animation-presets-spring)]
                 (layout-animation-configure-next type))))

(def native-methods-mixin (.-NativeMethodsMixin ReactNative))
(def net-info (.-NetInfo ReactNative))
(def pan-responder (.-PanResponder ReactNative))
(def pixel-ratio (.-PixelRatio ReactNative))
(def style-sheet (.-StyleSheet ReactNative))

(defn pan-responder-create [params]
  (comment {:on-start-should-set-pan-responder #()
            :on-move-should-set-pan-responder  #()
            :on-pan-responder-grant            #()
            :on-pan-responder-move             #()
            :on-pan-responder-release          #()
            :on-pan-responder-terminate        #()})
  (comment
    {:on-move-should-set-pan-responder          #()
     :on-move-should-set-pan-responder-capture  #()
     :on-start-should-set-pan-responder         #()
     :on-start-should-set-pan-responder-capture #()
     :on-pan-responder-reject                   #()
     :on-pan-responder-grant                    #()
     :on-pan-responder-start                    #()
     :on-pan-responder-end                      #()
     :on-pan-responder-release                  #()
     :on-pan-responder-move                     #()
     :on-pan-responder-terminate                #()
     :on-pan-responder-termination-request      #()
     :on-should-block-native-responder          #()})
  (-> pan-responder (.create (utils/prepare-to-js params))))

(defn animated-add [a b] (.add (.-Animated ReactNative) a b))
(defn animated-decay [value config] (.decay (.-Animated ReactNative) value config))
(defn animated-delay [time] (.delay (.-Animated ReactNative) time))
(defn animated-event [arg-mapping config] (.event (.-Animated ReactNative) arg-mapping config))
(defn animated-multiply [a b] (.multiply (.-Animated ReactNative) a b))
(defn animated-parallel [animations] (.parallel (.-Animated ReactNative) animations))
(defn animated-sequence [animations] (.sequence (.-Animated ReactNative) animations))
(defn animated-spring [value config] (.spring (.-Animated ReactNative) value config))
(defn animated-stagger [time animations] (.stagger (.-Animated ReactNative) time animations))
(defn animated-timing [value config] (.timing (.-Animated ReactNative) value config))
(defn animated-value [& args] (let [constructor (.-Value (.-Animated ReactNative))] (apply constructor. args)))
(defn animated-value-xy [& args] (let [constructor (.-ValueXY (.-Animated ReactNative))] (apply constructor. args)))

(def request-animation-frame js/requestAnimationFrame)
(def run-after-interactions (-> ReactNative .-InteractionManager .-runAfterInteractions))

;; custom

(defn spacer [s]
  [view {:style [(width s) (height s)]}])

(defn flexer
  ([] (flexer 1))
  ([f]
  [view {:style [(flex f)]}]))

(def default-button-style [(overflow "hidden")
                           (border 1 "rgba(0,0,0,0.2)")
                           (rounded 3) align-center
                           (padding 1)])

(defn button
  ([label] (button {} label))
  ([props label]
   (let [{:keys [on-press ref
                 background-color
                 disabled
                 tab-style
                 underlay-color
                 notification
                 style]}  props
         on-press         (or on-press #(println "default on-press function"))
         button-style     [default-button-style style
                           (when background-color (s/background background-color))
                           (case tab-style
                             "left"   (rounded 0 0 4 4)
                             "center" (rounded 0 0 0 0)
                             "right"  (rounded 4 4 0 0)
                             nil)]
         label            (if (string? label)
                            [text {:style [(padding 8) (font-size 13) bold]} label] label)
         button-component (if disabled view touchable-opacity)]
     [button-component
      {:ref            ref
       :on-press       (when-not disabled #(on-press %))
       :underlay-color (or underlay-color "rgba(0,0,0,0.2)")
       :style          button-style}

      [view
       label
       (when (and notification @notification)
         [view {:style [(overflow "hidden")
                        (top -2) (right -14)
                        (background red)
                        align-center (rounded 10)
                        s/position-absolute
                        (width 16) (height 16)]}
          [text {:style [bold (font-size 9) (margin 2 0) (color "white")]} @notification]])]])))

(defn toggle-button
  [props label]
  (let [{:keys [value state toggled]} props
        value                         (or value "toggled")
        state                         (or state (atom (if toggled value nil)))
        toggled                       (reaction (= @state value))]
    (fn [props]
      (let [{:keys [on-press ref another-value
                    disabled notification
                    tab-style style selected-style
                    background-color]} props
            style                      [default-button-style style]
            selected-style             (or selected-style (gray 4))]
        [button
         {:on-press         #(when-not disabled
                               (do
                                 (reset! state (if (= @state value) another-value value))
                                 (when on-press (on-press @state))))
          :notification     notification
          :disabled         disabled
          :ref              ref
          :background-color background-color
          :tab-style        tab-style
          :style            (conj [] style (when @toggled selected-style))}
         label]))))

(defn round-corners [w]
  (if (= "android" (.-OS Platform)) (* w 2) (/ w 2)))

(defn avatar-req
  ([url-req] (avatar-req url-req 60))
  ([url-req w]
   [view {:style [(rounded (/ w 2))
                  (gray 1) align-center
                  (overflow "hidden") (height w) (width w)]}
    [flexer]
    (if url-req
      [image {:style      [(width w) (height w)
                           (rounded (round-corners w))]
              :resizeMode "stretch"
              :source     url-req}]
      [text {:style [(font-size 9) (color "gray")]} "loading"])
    [flexer]]))

(defn rounded-button
  [props]
  (let [{:keys [w on-press disabled style]} props]
    [(if-not disabled touchable-opacity view)
     [view {:style [(rounded (/ w 2)) (gray 1)
                    align-center (overflow "hidden")
                    (height w) (width w) style]}
      [flexer]
      (into [view] (r/children (r/current-component)))
      [flexer]]]))

(defn avatar
  ([url] (avatar url 60))
  ([url w]

   [view {:style [(rounded (/ w 2)) (gray 1)
                  align-center (overflow "hidden")
                  (height w) (width w)]}
    [flexer]
    (if url
      [image {:style      [(width w) (height w)
                           (rounded (round-corners w))]
              :resizeMode "stretch"
              :source     {:uri url}}]
      [text {:style [(font-size 9) (color "gray")]} "loading"])
    [flexer]]))
