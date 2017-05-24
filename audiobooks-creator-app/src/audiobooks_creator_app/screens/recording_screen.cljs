(ns audiobooks-creator-app.screens.recording-screen
  (:require
   [reagent.core :as r :refer [atom]]
   [audiobooks-creator-app.events]
   [audiobooks-creator-app.subs]
   [micro-rn.components :as c :refer [view text alert]]
   [micro-rn.react-navigation :as nav]
   [audiobooks-creator-app.native-modules :as nm]))

(defn- screen-content []
  (fn []
    [view {:style {:flex            1
                   :justify-content "center"
                   :align-items     "center"}}
     [text "Content"]]))

(def main
  (nav/create-screen
   {:title "Recording"
    :tab-bar-icon (nm/TabIcon "ios-mic")
    }
   (screen-content)))


