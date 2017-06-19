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

(def test-text (apply str (repeat 1 (str "
    Четверг четвертого числа,
    В четверг четвертого числа,
    четыре с " (make-spaces 8) "четвертью числа" (make-spaces 18) "
    Лигурийский регулировщик регулировал в Лигурии
    Лигурийский регулировщик регулировал в Лигурии
"))))

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
  (let [word (subscribe [::model/word id])
        selected     (atom false)
        pos          (atom nil)
        gesture-data (atom nil)
        responder    (rn/pan-responder-create {:on-start-should-set-pan-responder #(do (println "Set pan" text) true)
                                               :on-pan-responder-grant            #(do (println "Grant" text)
                                                                                       (println (rn-util/event->pan-data %))
                                                                                       (reset! selected true))
                                               :on-pan-responder-move             #(do
                                                                                     (println "Move" text)
                                                                                     (println (rn-util/->getsture-state %2))
                                                                                     #_(println (rn-util/event->pan-data %1)))
                                               :on-pan-responder-release          #(do (println "Release" text)
                                                                                       (reset! selected false))})]
    (fn []
      (let [{:keys [text background-gray text-style]} @word
            text-style (-> text-style (conj (when @selected :invert)))
            text-style (-> text-style map-decorations)]
        [view (merge
               {:on-layout #(reset! pos (rn-util/event->layout %))
                :style     [(st/padding 2)
                            (when @selected (st/gray 9))
                            (when background-gray (st/gray 1))]}
               (some-> responder .-panHandlers js->clj))
         [rn/text {:style text-style} text]]))))

(defn icon-button [icon-name icon-text focused]
  [touchable-opacity {:style [(st/justify-content "center")
                              (st/align-items "center")]}
   [nm/icon-fa {:color "#ccc" :size 22 :name icon-name}]
   [text {:style [(st/color "#ccc") (st/font-size 8) (st/text-align "center")]} icon-text]])

(defn editor-toolbar []
  [view {:style [(st/flex)]}
   [flexer]
   [icon-button "search" "Find"]
   [flexer]
   [icon-button "eye-slash" "Hide deleted"]
   [flexer]
   [icon-button "strikethrough" "Mark as deleted"]
   [flexer]
   [icon-button "bold" "Volume +"]
   [flexer]
   [icon-button "italic" "Volume -"]
   [flexer]
   [icon-button "undo" "Undo"]
   [flexer]
   [icon-button "repeat" "Redo"]
   [flexer]])

(defn text-editor []
  (let [transcript (subscribe [::model/transcript])]
    (dispatch [::model/transcript
               [
                [{:text "В"}
                 {:text "четверг" :text-style [:u :b]}
                 {:text "четвертого" :text-style [:u]}
                 {:text "числа"}]
                [{:text "Четыре" :background-gray true}
                 {:text "c" :background-gray true}
                 {:text "четвертью" :background-gray true}
                 {:text "числа"}]
                [{:text "В" :text-style [:s]}
                 {:text "четверг" :text-style [:s]}
                 {:text "четвертого" :text-style [:s]}
                 {:text "числа" :text-style [:s]}]
                [{:text "Четыре" :selected true}
                 {:text "c" :selected true}
                 {:text "четвертью"}
                 {:text "числа"}]
                ]])

    (fn []
      [view {:style [(st/flex) st/row (st/background "white")]}
       [view {:style [(st/width 48)]}
        [editor-toolbar]]
       [view {:style [(st/gray 1) (st/width 1)]}]
       [view {:style [(st/padding 8 0 0 0)]}
        (let [c (atom 0)]
          (doall
           (for [x @transcript]
             ^{:key {:id (str "paragraph-" (swap! c inc))}}
             [p (doall
               (for [w x]
                 ^{:key {:id (str "word-" (:id w))}} [word (:id w)]))]
             )))]])))

(comment
  (subscribe [::model/word 1])
  (-> (subscribe [::model/word 1]) deref (get 1))
  (map-decorations [:u :b])
  (word {:text "четверг" :text-style [:u :b] :editable true :selected true})

  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

