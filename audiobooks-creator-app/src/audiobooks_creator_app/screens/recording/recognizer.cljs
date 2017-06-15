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
  (let [this (r/current-component)]
    (into [view {:style [st/row st/wrap (st/padding 4 8)]}] (r/children this))))

(defn map-decorations [values]
  (mapv #(case %
           :u st/underline    :b st/bold
           :s st/line-through :i st/italic nil) values))

(defn word [s & decorations]
  (let [decorations (set decorations)
        selected    (decorations :selected)
        editable    (decorations :editable)
        text-style  (map-decorations decorations)]
    [touchable-opacity {:style [(st/padding 2) (when selected (st/gray 1))]}
     (if-not editable
       [text {:style text-style} s]
       [text-input {:style text-style :value s}])]))

(defn text-editor []
  [view {:style [(st/flex) (st/background "white") (st/padding 8 0 0 0)]}
   [p
    [word "В"] [word "четверг" :u :b :editable :selected] [word "четвертого" :u] [word "числа"]]
   [p
    [word "В" :selected] [word "четверг" :selected] [word "четвертого" :selected] [word "числа"]]])

(comment
  (map-decorations [:u :b])
  (word "четверг" :u)
  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

