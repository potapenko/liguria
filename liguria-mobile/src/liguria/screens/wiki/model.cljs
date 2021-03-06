(ns liguria.screens.wiki.model
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
  (->> (range 1 20)
       (map #(do {:title (rand-nth ["Сценическая речь"
                                    "Подготовка к выступлению"
                                    "Обзор книг по публичным выступлениям"
                                    "Ритм в сценической речи"])
                  :id    %}))))

(reg-sub
 ::wiki-list
 (fn [db _]
   (get db ::wiki-list [])))

(reg-event-db
 ::wiki-list
 (fn [db [_ value]]
   (assoc db ::wiki-list value)))

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
     (assoc db ::data value))))
