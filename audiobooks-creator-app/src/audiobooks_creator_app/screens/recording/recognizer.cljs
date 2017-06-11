(ns audiobooks-creator-app.screens.recording.recognizer
  (:require [audiobooks-creator-app.shared.installed-components :as ic]
            [audiobooks-creator-app.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view spacer flexer]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [re-frame.core :refer [subscribe dispatch]]
            [audiobooks-creator-app.screens.recording.model :as model]))

;; react-native-speech-to-text-ios

(defn text-editor []
  (let [r (atom nil)]
    (fn []
      [view {:style [(st/flex) (st/background "red")]}
       [nm/rte-editor {:ref #(reset! r %)
                       :initialTitleHTML "My Title"
                       :initialContentHTML "Hello <b>World</b>
                                            <p>this is a new paragraph</p>
                                            <p>this is another new paragraph</p>"}]
       [nm/rte-toolbar {:get-editor #(do @r)}]])))
