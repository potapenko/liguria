(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db]]))

(reg-sub
 ::monitoring
 (fn [db _]
   (get db ::monitoring 0)))

(reg-event-db
 ::set-monitoring
 (fn [db [_ value]]
   (assoc db ::monitoring value)))
