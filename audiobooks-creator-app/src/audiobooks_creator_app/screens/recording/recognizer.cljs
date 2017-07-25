(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]
            [audiobooks-creator-app.screens.recording.rte-css :refer [css]]
            [audiobooks-creator-app.screens.recording.nlp :as nlp]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; react-native-speech-to-text-ios

(def editor-ref (atom nil))

(defn make-spaces [x]
  (apply str (repeat x " ")))

(def layout (atom nil))

(deref layout)

(defn paragraph [{:keys [id]}]
  (let [this      (r/current-component)
        props     (r/props this)
        on-layout (or (:on-layout props) identity)]
    (into [view {:on-layout #(on-layout (rn-util/event->layout %))
                 :style     [st/row st/wrap (st/padding 4 8)]}] (r/children this))))

(defn sentence [{:keys [id]}]
  (let [this      (r/current-component)
        props     (r/props this)
        on-layout (or (:on-layout props) identity)]
    (into [view {:on-layout #(on-layout (rn-util/event->layout %))
                 :style     [st/row st/wrap]}] (r/children this))))

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
                    selected
                    searched
                    ]}      @word
            selected        (and selected (= @mode :edit))
            background-gray (and (not selected) background-gray)
            text-style      (-> text-style (conj (when selected :invert)) map-decorations)
            view-ref        (atom nil)]
        (when-not (and (= @mode :search) (not searched))
         [view (merge
                {:ref       #(reset! view-ref %)
                 :on-layout #(rn-util/event->layout-ref @view-ref (fn [e] (dispatch [::model/word-data id :layout e])))
                 :style     [(st/padding 4 2)
                             (when selected (st/gray 9))
                             (when background-gray (st/gray 1))]}
                (rn-util/->gesture-props responder))
          [rn/text {:style (conj text-style (st/font-size 16))} text]])))))

(defn icon-button [icon-name icon-text focused]
  [touchable-opacity {:style [(st/justify-content "center")
                              (st/align-items "center")
                              (st/padding 12 0)]}
   [nm/icon-fa {:color "#ccc" :size 22 :name icon-name}]
   [text {:style [(st/color "#ccc") (st/font-size 8) (st/text-align "center")]} icon-text]])

(defn editor-toolbar []
  [view {:style [(st/width 60)]}
   [rn/scroll-view {:style [(st/flex)]}
    [icon-button "question-circle-o" "Help"]
    [flexer]
    [icon-button "eye-slash" "Hide deleted"]
    [flexer]
    [icon-button "strikethrough" "Mark as deleted"]
    [flexer]
    [icon-button "eraser" "Erase recording"]
    [flexer]
    [icon-button "undo" "Undo"]
    [flexer]
    [icon-button "repeat" "Redo"]]])

(defn text-editor []
  (let [transcript         (subscribe [::model/transcript])
        mode               (subscribe [::model/mode])
        select-in-progress (subscribe [::model/select-in-progress])]
    (dispatch [::model/transcript (nlp/create-text-parts nlp/test-text2)])
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [view {:style [st/row (st/flex)]}
        (when (= @mode :edit)
          [editor-toolbar])
        [view {:style [(st/gray 1) (st/width 1)]}]
        [rn/scroll-view {:style          [(st/padding-right 12)]
                         :scroll-enabled (not (and @select-in-progress (= @mode :edit)))
                         :on-scroll      #(dispatch [::model/scroll-pos (rn-util/scroll-y %)])}
         [text (str "id: " @(subscribe [::model/db [::model/count-click]]) #_", id: " #_@(subscribe [::model/prev-click]))]
         [rn/touchable-without-feedback {:on-press #(dispatch [::model/deselect])}
          [view {:style [(st/padding 0 0 0 0) (st/flex)]}
           (let [c (atom 0)]
             (doall
              (for [x @transcript]
                ^{:key {:id (str "paragraph-" (:id x))}}
                [paragraph x
                 (doall
                  (for [s (:sentences x)]
                    ^{:key {:id (str "sentence-" (:id s))}}
                    [sentence s
                     (doall
                      (for [w (:words s)]
                        ^{:key {:id (str "word-" (:id w))}}
                        [word (:id w)]))]))])))]]]]])))

(comment
  (subscribe [::model/words])
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

