(ns liguria.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync subscribe]]
            [micro-rn.rn-utils :as rn-utils]
            [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(reg-sub
 ::monitoring
 (fn [db _]
   (get db ::monitoring 0)))

(reg-event-db
 ::monitoring
 (fn [db [_ value]]
   (assoc db ::monitoring value)))

(reg-sub
 ::recording
 (fn [db _]
   (get db ::recording false)))

(reg-event-db
 ::recording
 (fn [db [_ value]]
   (assoc db
          ::mode (if value :record :idle)
          ::monitoring 0
          ::recording value)))

(reg-sub
 ::mode
 (fn [db _]
   #{:edit :search :record :idle}
   (get db ::mode :idle)))

(reg-event-db
 ::mode
 (fn [db [_ value]]
   (merge db
          {::mode value}
          (when (= value :idle) {::search-text ""}))))

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

