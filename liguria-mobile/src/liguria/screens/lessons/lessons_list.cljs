(ns liguria.screens.lessons.lessons-list
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
            [liguria.screens.lessons.model :as model])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn navigate!
  [screen props]
  (nav/navigate! @(subscribe [::model/navigator]) screen props))

(defn go-back! []
  (nav/go-back! @(subscribe [::model/navigator])))

(defn go-icon
  ([] (go-icon false))
  ([locked]
   [rn/view {:style [st/align-center st/justify-center]}
    (if-not locked
      [nm/icon-io {:color (st/gray-cl 2) :size 24 :name "ios-mic"}]
      [nm/icon-io {:color (st/gray-cl 2) :size 24 :name "ios-lock"}])]))

(defn lock-icon []
  [rn/view {:style [st/align-center st/justify-center]}
   [nm/icon-io {:color (st/gray-cl 2) :size 24 :name "ios-lock-outline"}]])

(defn star-icon
  ([] (star-icon false))
  ([higlited]
   [rn/view {:style [st/align-center st/justify-center (st/padding 1)]}
    (if-not higlited
      [nm/icon-io {:color (st/gray-cl 2) :size 16 :name "ios-star-outline"}]
      [nm/icon-io {:color "gold" :size 16 :name "ios-star"}])]))

(defn lessons-list-element [{:keys [id date title text color enabled statistic navigation]}]
  [view {:style []}
   [rn/touchable-opacity {:style    [st/row st/align-center (st/padding 16)]
                          :on-press #(navigate! :recording {:lesson id :text text})}
    [view {:style [(st/width 30)
                   (st/height 30)
                   (st/rounded 16)
                   (st/border 3 color)
                   st/align-center st/justify-center]}
     [rn/text {:style [st/bold]} (str (inc id))]]
    [rn/spacer 16]
    [rn/text {:number-of-lines 1 :elipsis-mode "tail"
              :style [(st/font-size 16) ]} (str title)]
    [view {:style [(st/flex) st/row (st/justify-content "flex-end")]}
     [view {:style [st/row (st/margin-top 2)]}
      [star-icon (-> statistic :accuracy (> 0))]
      [star-icon (-> statistic :accuracy (> 30))]
      [star-icon (-> statistic :accuracy (> 70))]]
     [spacer 16]
     [go-icon (not enabled)]]]])

(defn one-list-line [x]
  (let [id      (-> x .-item .-id)
        index   (-> x .-index)
        lessons (subscribe [::model/lessons-list])]
    (fn []
      ^{:key (str "lessons-" id)} [lessons-list-element (nth @lessons index)])))

(defn lessons-list [navigator]
  (let []
    (dispatch [::model/navigator navigator])
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [rn/flat-list {:data                   @(subscribe [::model/lessons-list])
                      :ItemSeparatorComponent #(r/as-element [view {:style [(st/height 1) (st/gray 10)]}])
                      :render-item            #(r/as-element [one-list-line %])
                      :key-extractor          #(str "lessons-list-" (-> % .-id))}]])))

(comment
  (navigate! :results {})
  @(subscribe [::model/lessons-list])
  @(subscribe [::model/navigator]))
