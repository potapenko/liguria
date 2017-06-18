(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db]]
            [clojure.core.reducers :as red]))

(reg-sub
 ::monitoring
 (fn [db _]
   (get db ::monitoring 0)))

(reg-event-db
 ::monitoring
 (fn [db [_ value]]
   (assoc db ::monitoring value)))

(reg-sub ::recording (fn [db _]
   (get db ::recording false)))

(reg-event-db
 ::recording
 (fn [db [_ value]]
   (assoc db ::monitoring 0)
   (assoc db ::recording value)))

(reg-sub
 ::transcript
 (fn [db _]
   (get db ::transcript [])))

(reg-event-db
 ::transcript
 (fn [db [_ value]]
   (let [c        (atom 0)
         with-ids (vec (for [p value]
                         (vec (for [w p]
                                (assoc w :id (swap! c inc))))))]
     (assoc db
            ::words (into {} (for [x (flatten with-ids)] {(:id x) x}))
            ::transcript (for [p with-ids]
                           (for [x with-ids] {:id (:id x)}))))))

(reg-sub
 ::word-data
 (fn [db id k]
   (get-in db [::words id k])))

(reg-event-db
 ::word-data
 (fn [db [_ id k v]]
   (assoc-in db [::words id k] v)))

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
