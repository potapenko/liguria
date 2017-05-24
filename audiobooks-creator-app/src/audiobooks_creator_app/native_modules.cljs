(ns audiobooks-creator-app.native-modules
  (:require
   [reagent.core :as r :refer [atom]]
   [reagent.impl.component :as ru]
   [clojure.string :as string]
   [micro-rn.components :as c]))

(def modules (js/require "./src/js/modules.js"))
(def TabIcon (.-TabIcon modules))

(def icons (.-icons modules))

(def icon-fa (r/adapt-react-class (.-IconFA icons)))
(def icon-md (r/adapt-react-class (.-IconMD icons)))
(def icon-io (r/adapt-react-class (.-IconIO icons)))
