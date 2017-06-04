(ns audiobooks-creator-app.server.utils
  (:require
   [reagent.core :as r :refer [atom]]
   [micro-rn.couchbase-lite :as cbl :refer [as-docs as-one-doc as-data-source aggregate <?
                                            get post delete put init view database document design-document attachment]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [audiobooks-creator-app.server.main :as server :refer [db]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(defn add-doc [doc]
  (-> db (post doc) <?))

(defn get-doc [id]
  (-> db (document id) (get) <?))

(defn update-doc
  ([id new-data]
   (let [port (chan)]
     (go
       (let [exists (<! (get-doc id))
             document (-> db (document id))]
         (if exists
           (-> document (put {:rev (:_rev exists)} (into exists new-data))  <?)
           (console.warn "[update-doc] document not exists: " id)))
       (>! port "[update-doc complete]"))
     port))
  ([doc]
   (let [port (chan)]
     (go
       (let [id (:_id doc)
             exists (<! (get-doc id))
             document (-> db (document id))]
         (if exists
           (-> document (put {:rev (:_rev exists)} doc)  <?)
           (-> document (put doc)  <?)))
       (>! port "[update-doc complete]"))
     port)))

(defn delete-doc [doc]
  (-> db (document (:_id doc)) (delete {:rev (:_rev doc)}) <?))

(defn get-view-docs
  ([view result-atom] (get-view-docs view result-atom {}))
  ([view result-atom params]
   (go
     (reset! result-atom (<! (-> view (cbl/get params) (cbl/as-list) <?))))))

(defn add-view [design-doc views]
  (-> design-doc (put {:language "javascript", :views views}) <?))
