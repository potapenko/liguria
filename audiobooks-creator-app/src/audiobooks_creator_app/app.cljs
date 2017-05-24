(ns audiobooks-creator-app.app
  (:require
   [reagent.core :as r :refer [atom]]
   [audiobooks-creator-app.events]
   [audiobooks-creator-app.subs]
   [micro-rn.components :as c :refer [view text alert]]
   [micro-rn.react-navigation :as nav]
   [audiobooks-creator-app.screens.projects-screen :as projects]
   [audiobooks-creator-app.screens.settings-screen :as settings]
   [audiobooks-creator-app.screens.about-screen :as about]
   [audiobooks-creator-app.screens.export-screen :as export]
   [audiobooks-creator-app.screens.recording-screen :as recording]
   [audiobooks-creator-app.screens.friends-screen :as friends]))

(def projects-stack
  (nav/create-stack-navigator
   {:editor-main {:screen projects/main}}))

(def friends-stack
  (nav/create-stack-navigator
   {:editor-main {:screen friends/main}}))

(def main-tabs
  (nav/create-tab-navigator
   {:projects-tab {:screen projects-stack}
    :friends-tab  {:screen friends/main}
    :settings-tab {:screen settings/main}
    :about-tab    {:screen about/main}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])
