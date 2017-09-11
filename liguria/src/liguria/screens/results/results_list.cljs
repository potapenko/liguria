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

(defn navigate!
  [screen props]
  (nav/navigate! @(subscribe [::model/navigator]) screen props))

(defn go-back! []
  (nav/go-back! @(subscribe [::model/navigator])))

(defn go-icon
  ([] (go-icon "ios-play" false))
  ([icon higlited]
   (let [color (if higlited "cornflowerblue" (st/gray-cl 2))
         w     32]
     [view {:style
            [st/align-center st/justify-center
             (st/overflow "hidden")
             (st/width w)
             (st/height w)
             (st/rounded (/ w 2))]}
      [nm/icon-io {:color color :size (- w 8) :name icon}]])))

(defn results-list-element [{:keys [id date statistic]}]
  [view {:style []}
   [rn/touchable-opacity {:style    [st/row st/align-center]
                          :on-press #(navigate! :result {:result id})}
    [rn/text {:number-of-lines 1 :elipsis-mode "tail"
              :style [(st/font-size 16) (st/padding 16)]} (str date)]
    [flexer]
    [go-icon]
    [spacer 16]]])

(defn one-list-line [x]
  (let [id      (-> x .-item .-id)
        index   (-> x .-index)
        results (subscribe [::model/results-list])]
    (fn []
      ^{:key (str "results-" id)} [results-list-element (nth @results index)])))

(defn results-list [navigator]
  (let []
    (dispatch [::model/navigator navigator])
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [rn/flat-list {:data                   @(subscribe [::model/results-list])
                      :ItemSeparatorComponent #(r/as-element [view {:style [(st/height 1) (st/gray 10)]}])
                      :render-item            #(r/as-element [one-list-line %])
                      :key-extractor          #(str "results-list-" (-> % .-id))}]])))

(comment
  @(subscribe [::model/results-list])
  @(subscribe [::model/navigator]))
