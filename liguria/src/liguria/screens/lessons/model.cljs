(ns liguria.screens.lessons.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync subscribe]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(defn build-test-data []
  (->> ["Лигурийский регулировщик"
        "Хохлатые хохотушки"
        "Сонька и Сашка"
        "Вавилонка Варвара"
        "Саша на шоссе"
        "Колокол переколоколовать"
        "Сеня с донесеньем"
        "У гусыни усов не ищи"
        "Не тот, товарищи, товарищу товарищ"
        "На улице дёготник"
        "Дело было в Вавилоне"
        "Конституционалист Константин"
        "Щеголь Щегол"
        "Лигурия"]
       (map-indexed (fn [index title]
                      (do {:id        index
                           :enabled   (< index 5)
                           :title     title
                           :statistic {:lesson (rand-int 200)
                                       :time   (rand-int 180)
                                       :errors (rand-int 200)}})))))

(reg-sub
 ::lessons-list
 (fn [db _]
   (get db ::lessons-list [])))

(reg-event-db
 ::lessons-list
 (fn [db [_ value]]
   (assoc db ::lessons-list value)))

(comment
  (reg-sub
   ::data
   (fn [db _]
     (get db ::data 0)))

  (reg-event-db
   ::data
   (fn [db [_ value]]
     (assoc db ::data value))))
