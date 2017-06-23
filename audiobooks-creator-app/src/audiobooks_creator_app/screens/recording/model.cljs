(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils])
  (:require-macros [micro-rn.macros :refer [...]]))

(defn map-words [db f]
  (assoc db ::words
         (reduce-kv (fn [m k v]
                      (assoc m k (f v))) {} (::words db))))

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
                           (for [x p] (select-keys x [:id])))))))

(reg-sub
 ::word
 (fn [db [_ id]]
   (get-in db [::words id])))

(reg-sub
 ::word-data
 (fn [db [_ id k]]
   (get-in db [::words id k])))

(defn set-word-data [db id k v]
  (assoc-in db [::words id k] v))

(reg-event-db
 ::word-data
 (fn [db [_ id k v]]
   (set-word-data db id k v)))

(defn deselect-all [db]
  (map-words db #(assoc % :selected false)))

(reg-event-db
 ::editor-on-layout
 (fn [db [_ id layout-data]]
   db))

(reg-event-db
 ::deselect
 (fn [db [_ id]]
   (-> db deselect-all)))

(reg-event-db
 ::word-click
 (fn [db [_ id gesture-state]]
   (-> db
       deselect-all
       (set-word-data id :selected true))))

(reg-event-db
 ::word-release
 (fn [db [_ id gesture-state]]
   (if (and
        (= (:prev-click db) id)
        (rn-utils/double-tap (:prev-gesture-state db) gesture-state))
     (do (dispatch [::word-double-click id])
         (assoc db :prev-gesture-state nil :prev-click id))
     (assoc db
            :prev-gesture-state gesture-state
            :prev-click id))))

(reg-event-db
 ::select-words-line
 (fn [db [_ id]]
   (let [clicked-y (-> db ::words (get id) :layout :page-y)]
     (map-words db #(assoc % :selected (= clicked-y (-> % :layout :page-y)))))))

(reg-event-db
 ::word-double-click
 (fn [db [_ id]]
   (dispatch [::select-words-line id])
   db))

(defn calculate-collision [words gesture-state]
  (let [{:keys [move-x move-y]} gesture-state]
    (->> words vals
         (filter (fn [w]
                   (let [{:keys [width height page-x page-y]} (:layout w)
                         left                                 page-x
                         right                                (+ page-x width)
                         top                                  page-y
                         bottom                               (+ page-y height)]
                     (and (<= left move-x right)
                          (<= top move-y bottom)))))
         first)))

(defn select-words-range [db from to]
  (map-words db #(assoc % :selected
                        (or (<= (:id from) (:id %) (:id to))
                            (<= (:id to) (:id %) (:id from))))))

(reg-event-db
 ::select-data
 (fn [db [_ word-id gesture-state]]
   (let [first-word    (-> db ::words (get word-id))
         last-selected (calculate-collision  (::words db) gesture-state)]
     (if last-selected
       (-> db
           (select-words-range first-word last-selected))
       db))))

(comment
  (reg-sub
   ::data
   (fn [db _]
     (get db ::data 0)))

  (reg-event-db
   ::data
   (fn [db [_ value]]
     (assoc db ::data value))))
