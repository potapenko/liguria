(ns liguria.screens.results.results-list
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [liguria.shared.screens-shared-ui :as sh]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.utils :as util]
            [liguria.screens.results.model :as model])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn result [{:keys [id date statistic]}]
  [view {:style [(st/border-bottom 1 (st/gray-cl 1))]}
   [rn/touchable-opacity {:style [st/row st/align-center (st/padding 16)]}
    [text {:style [(st/font-size 22)
                   (st/color "cornflowerblue")]} (str date)]
    [flexer]
    [sh/play-icon]]])

(defn one-list-line [x]
  (let [id      (-> x .-item .-id)
        index   (-> x .-index)
        results (subscribe [::model/results-list])]
    (fn []
      ^{:key (str "results-" id)} [result (nth @results index)])))

(defn results-list []
  (let []
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [rn/flat-list {
                      :data          @(subscribe [::model/results-list])
                      :render-item   #(r/as-element [one-list-line %])
                      :key-extractor #(str "results-list-" (-> % .-id))}]])))


(comment

  @(subscribe [::model/results-list])

  )
