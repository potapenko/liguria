(ns liguria.screens.recording.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.recording.recorder :as recorder]
            [liguria.screens.recording.model :as model]
            [liguria.screens.recording.recognizer :as rz]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [liguria.screens.recording.liguria-text :refer [liguria-text]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn- screen-content []
  (dispatch [::model/text-fragment liguria-text])
  (fn []
    [view {:style {:flex 1}}
     [recorder/monitor]
     [recorder/recording-controls]
     [rz/text-editor]]))

(def main
  (nav/create-screen
   {:title "Тренировка"
    :tab-bar-icon #(r/as-element [sh/icon-recording (util/prepare-to-clj %)])}
   (screen-content)))
