(ns liguria.screens.results.model
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
  (->> (range 1 10)
       (map #(do {:id        %
                  :date      (str "10-12-2017 10:" %)
                  :statistic {:result (rand-int 200)
                              :time   (rand-int 180)
                              :errors (rand-int 200)}}))))

(reg-sub
 ::results-list
 (fn [db _]
   (get db ::results-list [])))

(reg-event-db
 ::results-list
 (fn [db [_ value]]
   (assoc db ::results-list value)))

(comment
  (reg-sub
   ::data
   (fn [db _]
     (get db ::data 0)))

  (reg-event-db
   ::data
   (fn [db [_ value]]
     (assoc db ::data value))))
