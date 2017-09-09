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

(defn go-icon
  ([] (go-icon "ios-mic" false))
  ([icon disabled])
  (let [color (st/gray-cl 2)
        w     24]
    [view {:style
           [st/align-center st/justify-center
            (st/overflow "hidden")
            (st/width w)
            (st/height w)
            (st/rounded (/ w 2))
            (st/border 1 color)]}
     [nm/icon-io {:color color :size 18 :name "ios-mic"}]]))

(defn lesson [{:keys [id date title statistic]}]
  [rn/touchable-opacity {:style [st/row
                                 st/align-center
                 (st/border-bottom 1 (st/gray-cl 1))]}
   [text {:style [(st/font-size 16)
                  (st/padding 16)
                  #_(st/color "cornflowerblue")]} (str (inc id) ".   " title)]
   [flexer]
   [go-icon]
   [spacer 16]])

(defn one-list-line [x]
  (let [id      (-> x .-item .-id)
        index   (-> x .-index)
        lessons (subscribe [::model/lessons-list])]
    (fn []
      ^{:key (str "lessons-" id)} [lesson (nth @lessons index)])))

(defn lessons-list []
  (let []
    (fn []
      [view {:style [(st/flex) (st/background "white")]}
       [rn/flat-list {
                      :data          @(subscribe [::model/lessons-list])
                      :render-item   #(r/as-element [one-list-line %])
                      :key-extractor #(str "lessons-list-" (-> % .-id))}]])))


(comment

  @(subscribe [::model/lessons-list])

  )
