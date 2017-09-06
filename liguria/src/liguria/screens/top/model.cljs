(ns liguria.screens.top.model
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
       (map #(do {:name   (rand-nth ["Евгений Потапенко" "Василий Бовкин" "Елена Новицкая"])
                  :result (+ 1000 (rand-int 100))
                  :date   (str "10-12-2017 10:" %)
                  :info   "hello"}))
       (sort-by :result)
       reverse
       (map-indexed (fn [index item] (assoc item :id (inc index))))))

(reg-sub
 ::top-list
 (fn [db _]
   (get db ::top-list [])))

(reg-event-db
 ::top-list
 (fn [db [_ value]]
   (assoc db ::top-list value)))

(comment
  (reg-sub
   ::data
   (fn [db _]
     (get db ::data 0)))

  (reg-event-db
   ::data
   (fn [db [_ value]]
     (assoc db ::data value))))
