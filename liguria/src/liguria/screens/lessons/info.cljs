(ns liguria.screens.lessons.info
  (:require [micro-rn.styles :as st]
            [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [liguria.shared.screens-shared-ui :as sh]
            [reagent.core :as r]
            [micro-rn.utils :as utils]))

(defn screen-content []
  [rn/view {:style [(st/flex) (st/padding 8) (st/background-color "white")]}

   [rn/text {:style [st/bold (st/font-size 33)]} "Title"]
   [rn/spacer 16]
   [rn/text "text"]

   ])

(def main
  (nav/create-screen
   {:title        "Информация"
    :tab-bar-icon #(r/as-element [sh/icon-lessons (utils/prepare-to-clj %)])}
   (screen-content)))

