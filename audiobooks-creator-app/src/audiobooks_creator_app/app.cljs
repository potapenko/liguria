(ns audiobooks-creator-app.app
  (:require [audiobooks-creator-app.screens
             [about-screen :as about]
             [books-screen :as books]
             [export-screen :as export]
             [friends-screen :as friends]
             [recording-screen :as recording]
             [settings-screen :as settings]
             [bookshelf-screen :as bookshelf]]
            [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]))

(def books-stack
  (nav/create-stack-navigator
   {:books {:screen books/main}}))

(def friends-stack
  (nav/create-stack-navigator
   {:friends {:screen friends/main}}))

(def main-tabs
  (nav/create-tab-navigator
   {:books-tab {:screen books-stack}
    :recording-tab {:screen recording/main}
    :bookshelf-tab {:screen bookshelf/main}
    :friends-tab  {:screen friends/main}
    :about-tab    {:screen about/main}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])
