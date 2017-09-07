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

(defn wiki-element [{:keys [id title]}]
  (let [color "cornflowerblue"]
    [rn/touchable-opacity {:style [st/row (st/padding 16) (st/border-bottom 1 (st/gray-cl 1))]}
     [text {:style [(st/font-size 22) (st/color "cornflowerblue")]} (str title)]]))

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)
        wiki   (subscribe [::model/wiki-list])]
    (fn []
      ^{:key (str "wiki-" id)} [wiki-element (nth @wiki index)])))

(defn wiki-list []
  (let []
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [rn/flat-list {
                      :data          @(subscribe [::model/wiki-list])
                      :render-item   #(r/as-element [one-list-line %])
                      :key-extractor #(str "wiki-list-" (-> % .-id))}]])))
