(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util :refer [await]]
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
    <p><s>Четверг четвертого числа,</s></p>
    <p><u>В четверг четвертого числа,</u></p>
    <p><u>четыре с " (make-spaces 8) "четвертью числа" (make-spaces 18) "</u></p>
    <p><s>Лигурийский регулировщик регулировал в Лигурии</s></p>
    <p><u>Лигурийский регулировщик</u> регулировал в Лигурии</p>
"))))

(defn- stylize [st]
  (let [this (r/current-component)]
    (into [text (merge {:style st} (r/props this))] (r/children this))))

(defn selected []
  (stylize [st/line-through (st/gray 1) st/bold]))

(defn word [s]
  (let [selected? (atom false)]
    (fn []
      [(if @selected? selected text)
       {:on-long-press #(swap! selected? not)} s])))

(defn b []
  (stylize [st/bold]))

(defn i []
  (stylize [st/italic]))

(defn u []
  (stylize [st/underline (st/color "#9a9a9a")]))

(defn s []
  (stylize [st/line-through (st/color "#ccc")]))

(defn cr [c]
  (stylize [st/line-through (st/color c)]))


(defn space
  ([] (space 1))
  ([x] [word (make-spaces x)]))

(defn p [s]
  (let [this (r/current-component)]
    (into [view [text s]] (r/children this))))

(defn text-editor []
  [view {:style [(st/flex) (st/background "white") (st/padding 8 0 0 0)]}
   [view {:style [(st/padding 8)]}
    [text
     [u [word "В"] [space] [word "четверг"]
      [space 4]
      [selected [word "четвертого"]]
      [space 8]]
     [word "числа,"]]]])

(comment
  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

