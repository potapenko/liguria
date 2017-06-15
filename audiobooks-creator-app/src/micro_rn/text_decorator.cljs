(ns micro-rn.text-decorator
  (:require
   [reagent.core :as r :refer [atom]]
   [micro-rn.rn-utils :as rn-util]
   [micro-rn.react-native :as rn :refer [alert text text-input view spacer flexer]]
   [micro-rn.styles :as st]))

(defn- stylize [st]
  (let [this (r/current-component)]
    (into [text (merge {:style st} (r/props this))] (r/children this))))

(defn selected []
  (stylize [st/line-through (st/gray 1) st/bold]))

(defn word [s]
  (let [selected? (atom false)
        pos       (atom "nothing")]
    (fn []
      [(if @selected? selected text)
       {:on-layout      #(reset! pos (rn-util/event->layout %))
        :on-long-press2 #(swap! selected? not)} (str @pos)])))

(defn b []
  (stylize [st/bold]))

(defn i []
  (stylize [st/italic]))

(defn u []
  (stylize [st/underline (st/color "#9a9a9a")]))

(defn s []
  (stylize [st/line-through (st/color "#ccc")]))

(defn cr [c]
  (stylize [st/line-through (st/color c)]))

(defn make-spaces [x]
  (apply str (repeat x " ")))

(defn space
  ([] (space 1))
  ([x] [word (make-spaces x)]))

(defn p [s]
  (let [this (r/current-component)]
    (into [view [text s]] (r/children this))))

(comment
  [text
   [word "!!!"]
   [u [word "В"] [space] [word "четверг"]
    [space 4]
    [selected [word "четвертого"]]
    [space 8]]
   [word "числа,"]])
