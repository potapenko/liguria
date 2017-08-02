(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [audiobooks-creator-app.screens.recording.model :as model]
            [audiobooks-creator-app.screens.recording.rte-css :refer [css]]
            [audiobooks-creator-app.screens.recording.nlp :as nlp]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.utils :as util])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; react-native-speech-to-text-ios

(def editor-ref (atom nil))

(defn make-spaces [x]
  (apply str (repeat x " ")))

(defn paragraph [{:keys [id]}]
  (let [this      (r/current-component)
        props     (r/props this)
        sentences (subscribe [::model/paragraph-data id :sentences])
        layout    (subscribe [::model/paragraph-data id :layout])]
    (fn []
      (let [hide? (every? #(:hidden %) @sentences)]
        (when-not hide?
          (into [view {:on-layout #(dispatch [::model/paragraph-data id :layout (rn-util/event->layout %)])
                       :style     [(when @layout (st/layout->size @layout))
                                   (st/padding 6 12)
                                   (st/border 1 (st/gray-cl 1) "solid")
                                   (st/border-left 0)
                                   (st/border-right 0)
                                   (st/border-top 0)]}] (r/children this)))))))

(defn sentence [{:keys [id]}]
  (let [this    (r/current-component)
        props   (r/props this)
        hidden? (subscribe [::model/sentence-data id :hidden])]
    (fn []
      (when-not @hidden?
        (into [view {:on-layout #(dispatch [::model/sentence-data id :layout (rn-util/event->layout %)])
                     :style     [(st/width "100%")
                                 (st/margin 6 0)
                                 st/row st/wrap]}] (r/children this))))))

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
        text-size    (subscribe [::model/text-size])
        gesture-data (atom nil)
        responder    (rn/pan-responder-create
                      {:on-start-should-set-pan-responder #(do (println "on-start" id) true #_(= @mode :edit))
                       :on-pan-responder-grant            #(dispatch [::model/word-click id (rn-util/->getsture-state %2)])
                       :on-pan-responder-move             #(dispatch [::model/select-progress id (rn-util/->getsture-state %2)])
                       :on-pan-responder-release          #(dispatch [::model/word-release id (rn-util/->getsture-state %2)])})]
    (fn []
      (let [{:keys [text
                    background-gray
                    text-style
                    selected
                    searched]} @word
            selected           (and selected #_(= @mode :edit))
            background-gray    (and (not selected) background-gray)
            text-style         (-> text-style (conj (when selected :invert)) map-decorations)
            view-ref           (atom nil)]
        [view (merge
               {:ref   #(do (reset! view-ref %) (dispatch [::model/word-data id :ref %]))
                ;; :on-layout #(rn-util/ref->layout @view-ref (fn [e] (dispatch [::model/word-data id :layout e])))
                :style [(st/padding 4 2)
                        (when selected (st/gray 9))
                        (when background-gray (st/gray 1))]}
               (rn-util/->gesture-props responder))
         [rn/text {:style (conj text-style (st/font-size @text-size))} text]]))))

(defn word-empty [text id]
  (let []
    (dispatch [::model/word-data id :ref nil])
    (fn [] [view {:style [(st/padding 4 2)]}
            [rn/text {:style [(st/font-size @(subscribe [::model/text-size]))]} text]])))

(defn icon-button [icon-name icon-text focused]
  [touchable-opacity {:style [(st/justify-content "center")
                              (st/align-items "center")
                              (st/padding 22 0 0 0)]}
   [nm/icon-fa {:color "#ccc" :size 22 :name icon-name}]
   [text {:style [(st/color "#ccc") (st/font-size 8) (st/text-align "center")]} icon-text]])

(defn editor-toolbar []
  [view {:style [(st/width 60)]}
   [rn/scroll-view {:style [(st/flex)]}
    [icon-button "question-circle-o" "Help"]
    [icon-button "eye-slash" "Hide deleted"]
    [icon-button "strikethrough" "Mark as deleted"]
    [icon-button "eraser" "Erase recording"]
    [icon-button "undo" "Undo"]
    [icon-button "repeat" "Redo"]]])

(defn one-paragraph [x]
  (let [item    (-> x .-item utils/prepare-to-clj)
        index   (-> x .-index)
        layout  (atom nil)
        hidden? (subscribe [::model/paragraph-data (:id item) :hidden])]
    ;; (println "build one paragraph:" index)
    (fn []
      [rn/touchable-opacity {:active-opacity 1
                             :on-press       #(dispatch [::model/paragraph-click (:id item)])}
       [paragraph item
        (doall
         (for [s (:sentences item)]
           ^{:key {:id (str "sentence-" (:id s))}}
           [sentence s
            (doall
             (for [w (:words s)]
               (if-not @hidden?
                 ^{:key {:id (str "word-" (:id w))}} [word (:id w)]
                 ^{:key {:id (str "word-" (:id w))}} [word-empty (:text w) (:id w)])))]))]
       [view {:style [(st/width (:width @layout)) (st/height (:height @layout))]}]])))

(defn text-editor []
  (let [transcript         (subscribe [::model/transcript])
        mode               (subscribe [::model/mode])
        select-in-progress (subscribe [::model/select-in-progress])
        words-ids          (subscribe [::model/words-ids])]
    (dispatch [::model/text-fragment nlp/test-text3])
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [view {:style [st/row (st/flex)]}
        [editor-toolbar]
        [view {:style [(st/gray 1) (st/width 1)]}]
        [rn/flat-list {:ref                       #(dispatch [::model/list-ref %])
                       :on-layout                 #(dispatch [::model/list-layout-event])
                       :remove-clipped-subviews   true
                       :initial-num-to-render     5
                       :on-viewable-items-changed (fn [data]
                                                    (doseq [[id visible] (->> data .-changed
                                                                              (map (fn [e] [(-> e .-item .-id)
                                                                                            (-> e .-isViewable)])))]
                                                      (dispatch [::model/paragraph-hidden id (not visible)]))
                                                    (dispatch [::model/update-words-layouts]))
                       :data                      @transcript
                       :render-item               #(r/as-element [one-paragraph %])
                       :key-extractor             #(str "paragraph-" (-> % .-id))}]
        ]])))

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

