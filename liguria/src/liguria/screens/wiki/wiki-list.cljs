(ns liguria.screens.wiki.resulsts-list
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [micro-rn.text-decorator :as text-decorator]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.utils :as util])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn result [{:keys [id]}]
  [view [text "one element"]])

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)]
    (fn []
      ^{:key (str "wiki-" id)} [result {:id id}])))

(defn wiki-list []
  [view {:style [(st/flex) (st/background "white")]}
   [rn/flat-list {
                  :data          []
                  :render-item   #(r/as-element [one-list-line %])
                  :key-extractor #(str "wiki-list-" (-> % .-id))}]])
