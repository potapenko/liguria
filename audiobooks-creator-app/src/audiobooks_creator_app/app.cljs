(ns audiobooks-creator-app.app
  (:require
   [reagent.core :as r :refer [atom]]
   [audiobooks-creator-app.events]
   [audiobooks-creator-app.subs]
   [micro-rn.components :as c :refer [view text alert]]))

(defn app-root-component []
  (fn []
    [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
     [text "hello!!!!!"]]))
