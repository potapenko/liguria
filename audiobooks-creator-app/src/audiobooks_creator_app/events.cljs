(ns audiobooks-creator-app.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [audiobooks-creator-app.db :as db :refer [app-db]]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

