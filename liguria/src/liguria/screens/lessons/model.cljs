(ns liguria.screens.lessons.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync subscribe]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [liguria.shared.liguria-text :refer [liguria-text]]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [liguria.shared.nlp :as nlp])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(defn build-test-data []
  (let [paragraphs (nlp/create-paragraphs liguria-text)]
    (->> [["Лигурийский регулировщик" "orangered"]
          ["Хохлатые хохотушки" "orange"]
          ["Сонька и Сашка" "gold"]
          ["Вавилонка Варвара" "greenyellow"]
          ["Саша на шоссе" "deepskyblue"]
          ["Колокол переколоколовать" "#A16BD3" #_"blueviolet"]
          ["Сеня с донесеньем" "dodgerblue"]
          ["У гусыни усов не ищи" "#FF65B8" #_"deeppink"]
          ["Береги честь с молоду" "orange"]
          ["Конституционалист Константин" "orangered"]
          ["Щёголь Щегол" "gold"]
          ["Лигурия" "gray"]]
         (map-indexed (fn [index [title color]]
                        (do {:id        index
                             :enabled   (< index 5)
                             :title     title
                             :color     color
                             :text      (or (nth paragraphs index nil) liguria-text)
                             :statistic {:lesson   (rand-int 200)
                                         :accuracy (-> 50 rand-int (+ 10))
                                         :time     (rand-int 180)
                                         :errors   (rand-int 200)}}))))))

(reg-sub
 ::lessons-list
 (fn [db _]
   (get db ::lessons-list [])))

(reg-event-db
 ::lessons-list
 (fn [db [_ value]]
   (assoc db ::lessons-list value)))

(reg-sub
 ::navigator
 (fn [db _]
   (get db ::navigator 0)))

(reg-event-db
 ::navigator
 (fn [db [_ value]]
   (assoc db ::navigator value)))

(comment
  (reg-sub
   ::data
   (fn [db _]
     (get db ::data 0)))

  (reg-event-db
   ::data
   (fn [db [_ value]]
     (assoc db ::data value)))
  )
