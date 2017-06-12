(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view spacer flexer]]
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
  (apply str (repeat x " &nbsp; ")))

(def test-text (apply str (repeat 1 (str "
    <p><s>Четверг четвертого числа,</s></p>
    <p><u>В четверг четвертого числа,</u></p>
    <p><u>четыре с " (make-spaces 8) "четвертью числа" (make-spaces 18) "</u></p>
    <p><s>Лигурийский регулировщик регулировал в Лигурии</s></p>
    <p><u>Лигурийский регулировщик</u> регулировал в Лигурии</p>
"))))

(defn text-editor []
  [view {:style [(st/flex) (st/background "white") (st/padding 8 0 0 0)]}
   [nm/rte-editor {:ref                #(reset! editor-ref %)
                   :style              [(st/flex 1) (st/margin 0 0 0 8)]
                   :customCSS          css
                   :titlePlaceholder   "Record title"
                   :contentPlaceholder "Paste subtitle text here"
                   :initialTitleHTML   "My record #1"
                   :initialContentHTML test-text}]
   #_[nm/rte-toolbar {:actions    [
                                 nm/rte-actions.setBold
                                 nm/rte-actions.setItalic
                                 ;; nm/rte-actions.setUnderline
                                 ;; nm/rte-actions.setStrikethrough
                                 ;; nm/rte-actions.setTextColor
                                 ]
                    :get-editor #(do @editor-ref)}]])

(comment
  (-> @editor-ref (.showTitle false))
  (go
    (let [[err res] (<! (await (-> @editor-ref .getContentHtml)))]
      (println ">>>" res))))

