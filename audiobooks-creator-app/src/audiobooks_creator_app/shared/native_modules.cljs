(ns audiobooks-creator-app.shared.native-modules
  (:require [clojure.string :as string]
            [micro-rn.react-native :as c]
            [reagent.core :as r :refer [atom]]
            [reagent.impl.component :as ru]))

(def modules (js/require "./src/js/modules.js"))
(def TabIcon (.-TabIcon modules))

(def icons (.-icons modules))

(def icon-fa (r/adapt-react-class (.-IconFA icons)))
(def icon-md (r/adapt-react-class (.-IconMD icons)))
(def icon-io (r/adapt-react-class (.-IconIO icons)))

(def couchbase (.-Couchbase modules))
