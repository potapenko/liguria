(ns liguria.screens.wiki.wiki-list
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [micro-rn.text-decorator :as text-decorator]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [liguria.screens.wiki.model :as model])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn one-wiki-element [{:keys [id]}]
  [view [text "one element"]])

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)]
    (fn []
      ^{:key (str "wiki-" id)} [one-wiki-element {:id id}])))

(defn wiki-list []
  [view {:style [(st/flex) (st/background "white")]}
   [rn/flat-list {
                  :data          @(subscribe [::model/wiki-list])
                  :render-item   #(r/as-element [one-list-line %])
                  :key-extractor #(str "wiki-list-" (-> % .-id))}]])
