(ns liguria.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [liguria.db :as db :refer [app-db]]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))
