(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util :refer [await]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]
            [audiobooks-creator-app.screens.recording.rte-css :refer [css]]
            [micro-rn.text-decorator :refer [word space b p u s selected make-spaces]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; react-native-speech-to-text-ios

(def editor-ref (atom nil))

(def test-text (apply str (repeat 1 (str "
    <p><s>Четверг четвертого числа,</s></p>
    <p><u>В четверг четвертого числа,</u></p>
    <p><u>четыре с " (make-spaces 8) "четвертью числа" (make-spaces 18) "</u></p>
    <p><s>Лигурийский регулировщик регулировал в Лигурии</s></p>
    <p><u>Лигурийский регулировщик</u> регулировал в Лигурии</p>
"))))

(def layout (atom nil))

(deref layout)

(defn text-editor []
  [view {:style [(st/flex) (st/background "white") (st/padding 8 0 0 0)]}
   [view {:style [(st/padding 8)]}
    [text {:on-layout #(reset! layout (rn-util/event->layout %))} "hello"]
    [text
     [word "!!!"]
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

