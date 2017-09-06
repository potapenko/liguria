(ns liguria.screens.top.top-list
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [liguria.screens.top.model :as model]
            [liguria.shared.screens-shared-ui :as sh])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn top-element [{:keys [id date name result info]}]
  (let [color "cornflowerblue"]
    [rn/touchable-opacity {:style [st/row
                                   st/align-center
                                   (st/padding 16)
                                   (st/border-bottom 1 (st/gray-cl 1))]}
     [text {:style [(st/font-size 22) (st/color color)]} (str id)]
     [spacer 16]
     [text {:style [(st/font-size 22) (st/color color)]} (str name)]
     [flexer]
     [text {:style [(st/color color)]} (str result)]
     [spacer 16]
     [sh/play-icon]]))

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)
        top   (subscribe [::model/top-list])]
    (fn []
      ^{:key (str "top-" id)} [top-element (nth @top index)])))

(defn top-list []
  (let []
    (dispatch [::model/top-list (model/build-test-data)])
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [rn/flat-list {:data          @(subscribe [::model/top-list])
                      :render-item   #(r/as-element [one-list-line %])
                      :key-extractor #(str "top-list-" (-> % .-id))}]])))
