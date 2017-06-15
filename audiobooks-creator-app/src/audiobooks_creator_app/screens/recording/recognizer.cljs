(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util :refer [await]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]
            [audiobooks-creator-app.screens.recording.rte-css :refer [css]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]])
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
               :selected (st/color "white")
               :s        [st/line-through (st/color "#ccc")]
               :u        st/underline
               :b        st/bold
               :i        st/italic nil) values)

       (filter #(-> % nil? not)) flatten vec))

(defn word [{:keys [text background-gray text-style selected editable on-press on-layout]}]
  (let [text-style (-> text-style (conj (when selected :selected)))
        text-style (-> text-style map-decorations)
        on-layout  (or on-layout identity)]
    [touchable-opacity {:on-layout #(on-layout (rn-util/event->layout %))
                        :style     [(st/padding 2)
                                    (when selected (st/gray 9))
                                    (when background-gray (st/gray 1))]
                        :on-press  on-press}
     [rn/text {:style text-style} text]]))

(defn text-editor []
  [view {:style [(st/flex) (st/background "white") (st/padding 8 0 0 0)]}
   [p
    [word {:text "В"}]
    [word {:text "четверг" :text-style [:u :b]}]
    [word {:text "четвертого" :text-style [:u]}]
    [word {:text "числа"}]]
   [p
    [word {:text "Четыре" :background-gray true}]
    [word {:text "c" :background-gray true}]
    [word {:text "четвертью" :background-gray true}]
    [word {:text "числа"}]]
   [p
    [word {:text "В" :text-style [:s]}]
    [word {:text "четверг" :text-style [:s]}]
    [word {:text "четвертого" :text-style [:s]}]
    [word {:text "числа" :text-style [:s]}]]
   [p
    [word {:text "Четыре" :selected true}]
    [word {:text "c" :selected true}]
    [word {:text "четвертью"}]
    [word {:text "числа"}]]])

(comment
  (map-decorations [:u :b])
  (word {:text "четверг" :text-style [:u :b] :editable true :selected true})

  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

