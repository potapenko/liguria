(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db]]
            [clojure.core.reducers :as red])
  (:require-macros [micro-rn.macros :refer [...]]))

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

(reg-event-db
 ::start-select
 (fn [db [_ id]]
   ;; (println "start-select:" id)
   (-> db (set-word-data id :selected true))))

(reg-event-db
 ::end-select
 (fn [db [_ id]]
   ;; (println "end-select:" id)
   (-> db (set-word-data id :selected false))))

(defn deselect-all [db])

(defn calculate-collision [words gesture-state]
  (let [{:keys [move-x move-y]} gesture-state]
    (->> words vals
         (filter (fn [w]
                   (let [{:keys [width height page-x page-y]} (:layout w)
                         left                                 page-x
                         right                                (+ page-x width)
                         top                                  page-y
                         bottom                               (+ page-y height)]
                     (when  (and (<= left move-x right)
                                 (<= top move-y bottom))
                       (println (... left move-x right) (... top move-y bottom) (:text w)))
                     (and (<= left move-x right)
                          (<= top move-y bottom)))))
         first)))

(reg-event-db
 ::select-data
 (fn [db [_ word-id gesture-state]]
   ;; (println "select-data:" word-id gesture-state)
   (let [first-word    (-> db ::words (get word-id))
         last-selected (calculate-collision  (::words db) gesture-state)]
     (println (... last-selected))
     (->
      db
      #_(assoc (map)))
     ;; найти слово в коордиднатах
     ;; убрать селекшен
     ;; выделить все слова от первого до этого
     )db))

(comment
  (reg-sub
   ::data
   (fn [db _]
     (get db ::data 0)))

  (reg-event-db
   ::data
   (fn [db [_ value]]
     (assoc db ::data value))))
