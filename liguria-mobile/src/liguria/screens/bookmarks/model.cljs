(ns liguria.screens.bookmarks.model
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
  (->> ["переколоколовать, перевыколоколовать" "пневмомешковыколачиватёли" "бомбами забомбардирует" "размокропогодилась"]
       (map-indexed (fn [index text] {:text text :id index}))))

(reg-sub
 ::bookmarks-list
 (fn [db _]
   (get db ::bookmarks-list [])))

(reg-event-db
 ::bookmarks-list
 (fn [db [_ value]]
   (assoc db ::bookmarks-list value)))

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
