(ns liguria.screens.recording.views
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text view]]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.utils :as util]
            [liguria.shared.screens-shared-ui :as sh]
            [liguria.screens.recording.controls :as controls]
            [liguria.screens.recording.model :as model]
            [liguria.recognizer.recognizer :as recognizer]
            [liguria.shared.liguria-text :refer [liguria-text]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn icon-recording [{:keys [tint-color focused] :as props}]
  [sh/tab-icon tint-color focused "ios-mic-outline"])

(defn- screen-content []
  (fn []
    [view {:style {:flex 1}}
     [controls/monitor]
     [controls/recording-controls]
     [recognizer/text-editor liguria-text]]))

(def main
  (nav/create-screen
   {:title "Тренировка"
    :tab-bar-icon #(r/as-element [icon-recording (util/prepare-to-clj %)])}
   (screen-content)))
