(ns liguria.screens.results.resulsts-list
  (:require [liguria.shared.native-modules :as nm]
            [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer touchable-opacity]]
            [micro-rn.styles :as st]
            [micro-rn.react-navigation :as nav]
            [micro-rn.text-decorator :as text-decorator]
            [reagent.core :as r :refer [atom]]
            [micro-rn.rn-utils :as rn-util]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [liguria.screens.recording.model :as model]
            [liguria.screens.recording.rte-css :refer [css]]
            [liguria.screens.recording.nlp :as nlp]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.utils :as util]
            [liguria.screens.recording.liguria-text :refer [liguria-text]])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(defn result [{:keys [id]}]
  [view [text "one result"]])

(defn one-list-line [x]
  (let [id    (-> x .-item .-id)
        index (-> x .-index)]
    (fn []
      ^{:key (str "resulst-" id)} [result {:id id}])))

(defn results-list []
  [view {:style [(st/flex) (st/background "white")]}
   [rn/flat-list {
                  :data                      []
                  :render-item               #(r/as-element [one-list-line %])
                  :key-extractor             #(str "resulst-list-" (-> % .-id))}]])
