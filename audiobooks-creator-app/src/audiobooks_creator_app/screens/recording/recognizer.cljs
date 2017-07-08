(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util :refer [await]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]
            [audiobooks-creator-app.screens.recording.rte-css :refer [css]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; react-native-speech-to-text-ios

(def editor-ref (atom nil))

(defn make-spaces [x]
  (apply str (repeat x " ")))

(def layout (atom nil))

(deref layout)

(defn p []
  (let [this      (r/current-component)
        props     (r/props this)
        on-layout (or (:on-layout props) identity)]
    (into [view {:on-layout #(on-layout (rn-util/event->layout %))
                 :style     [st/row st/wrap (st/padding 4 8)]}] (r/children this))))

(defn- map-decorations [values]
  (->> (map #(case %
               :invert (st/color "white")
               :s      [st/line-through (st/color "#ccc")]
               :u      st/underline
               :b      st/bold
               :i      st/italic nil) values)

       (filter #(-> % nil? not)) flatten vec))

(defn word [id]
  (let [word         (subscribe [::model/word id])
        mode         (subscribe [::model/mode])
        gesture-data (atom nil)
        responder    (rn/pan-responder-create
                      {:on-start-should-set-pan-responder #(= @mode :edit)
                       :on-pan-responder-grant            #(dispatch [::model/word-click id (rn-util/->getsture-state %2)])
                       :on-pan-responder-move             #(dispatch [::model/select-data id (rn-util/->getsture-state %2)])
                       :on-pan-responder-release          #(dispatch [::model/word-release id (rn-util/->getsture-state %2)])})]
    (fn []
      (let [{:keys [text
                    background-gray
                    text-style
                    selected]} @word
            selected           (and selected (= @mode :edit))
            background-gray    (and (not selected) background-gray)
            text-style         (-> text-style (conj (when selected :invert)) map-decorations)
            view-ref           (atom nil)]
        [view (merge
               {:ref       #(reset! view-ref %)
                :on-layout #(rn-util/event->layout-ref @view-ref (fn [e] (dispatch [::model/word-data id :layout e])))
                :style     [(st/padding 4 2)
                            (when selected (st/gray 9))
                            (when background-gray (st/gray 1))]}
               (rn-util/->gesture-props responder))
         [rn/text {:style (conj text-style (st/font-size 16))} text]]))))

(defn icon-button [icon-name icon-text focused]
  [touchable-opacity {:style [(st/justify-content "center")
                              (st/align-items "center")
                              (st/padding 8)]}
   [nm/icon-fa {:color "#ccc" :size 22 :name icon-name}]
   [text {:style [(st/color "#ccc") (st/font-size 8) (st/text-align "center")]} icon-text]])

(defn editor-toolbar []
  [view {:style [(st/flex)]}
   [icon-button "question-circle-o" "Help"]
   [flexer]
   [icon-button "eye-slash" "Hide deleted"]
   [flexer]
   [icon-button "strikethrough" "Mark as deleted"]
   [flexer]
   [icon-button "bold" "Bold"]
   [flexer]
   [icon-button "italic" "Italic"]
   [flexer]
   [icon-button "undo" "Undo"]
   [flexer]
   [icon-button "repeat" "Redo"]])

(defn text-editor []
  (let [transcript (subscribe [::model/transcript])
        mode       (subscribe [::model/mode])]
    (dispatch [::model/transcript
               [[{:text "В"}
                 {:text "четверг" :text-style [:u :b]}
                 {:text "четвертого" :text-style [:u]}
                 {:text "числа"}
                 {:text "четыре" :background-gray true}
                 {:text "c" :background-gray true}
                 {:text "четвертью" :background-gray true}
                 {:text "числа"}]
                [{:text "В" :text-style [:s]}
                 {:text "четверг" :text-style [:s]}
                 {:text "четвертого" :text-style [:s]}
                 {:text "числа" :text-style [:s]}
                 {:text "четыре" :selected true}
                 {:text "c" :selected true}
                 {:text "четвертью"}
                 {:text "числа"}]]])

    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [view {:style [st/row (st/flex)]}
        (when (= @mode :edit)
          [view {:style [(st/width 48)]}
           [editor-toolbar]])
        [view {:style [(st/gray 1) (st/width 1)]}]
        [rn/touchable-without-feedback {:on-press #(dispatch [::model/deselect])}
         [view {:style [(st/padding 0 0 0 0) (st/flex)]}
          (let [c (atom 0)]
            (doall
             (for [x @transcript]
               ^{:key {:id (str "paragraph-" (swap! c inc))}}
               [p (doall
                   (for [w x]
                     ^{:key {:id (str "word-" (:id w))}} [word (:id w)]))])))]]]
       #_[view {:style [(st/height 60) (st/gray 1)]}
        [text-input {:style [(st/flex)
                             (st/padding 16)] :placeholder "Input text here"}]]])))

(comment
  (subscribe [::model/word-data 1 :selected])
  (subscribe [::model/word-data 1 :layout])
  (subscribe [::model/word 13])
  (-> (subscribe [::model/word 1]) deref (get 1))
  (map-decorations [:u :b])
  (word {:text "четверг" :text-style [:u :b] :editable true :selected true})

  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

