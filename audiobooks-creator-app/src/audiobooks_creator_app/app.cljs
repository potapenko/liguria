(ns audiobooks-creator-app.app
  (:require
   [reagent.core :as r :refer [atom]]
   [audiobooks-creator-app.events]
   [audiobooks-creator-app.subs]
   [micro-rn.components :as c :refer [view text alert]]
   [micro-rn.react-navigation :as nav]))

(defn main-screen-content []
  (fn []
    [view [text "hello"]]))

(defn get-main-screen []
  (nav/create-screen
   {:title "Main"}
   (main-screen-content)))

(def main-stack
  (nav/create-stack-navigator
   {:editor-main {:screen (get-main-screen)}}))

(def main-tabs
  (nav/create-tab-navigator
   {:main-tab  {:screen main-stack}
    :about-tab {:screen main-stack}}))

(def main-wrapper
  (r/adapt-react-class main-stack))

(defn app-root-component []
  [main-wrapper])
