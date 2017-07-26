(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils]
            [clojure.string :as string]
            [audiobooks-creator-app.screens.recording.nlp :as nlp])
  (:require-macros [micro-rn.macros :refer [...]]))

;; -------------------------------------------------------------------------------

(defn map-words [db f]
  (assoc db ::words
         (reduce-kv (fn [m k v]
                      (assoc m k (f v))) {} (::words db))))

(defn get-word-data [db id k]
  (get-in db [::words id k]))

(defn set-word-data [db id k v]
  (assoc-in db [::words id k] v))

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

(defn deselect-all [db]
  (map-words db #(assoc % :selected false)))

(defn select-words-range [db from to]
  (map-words db #(assoc % :selected
                        (or (<= (:id from) (:id %) (:id to))
                            (<= (:id to) (:id %) (:id from))))))

(defn filter-searched [db text]
  (let [rx (re-pattern (str (string/lower-case text) ".+"))]
    db

    ))

(defn get-words-line [db word-id]
  )

(defn get-sentence [db word-id]
  )

(defn select-line [db id]
  (let [word-y (-> db ::words (get id) :layout :page-y)]
    (map-words db #(assoc % :selected (= word-y (-> % :layout :page-y))))))

(defn select-sentence [db id]
  (let [s-id (get-word-data db id :s-id)]
    (map-words db #(assoc % :selected (= s-id (-> % :s-id))))))

(defn select-paragraph [db id]
  (let [p-id (get-word-data db id :p-id)]
    (map-words db #(assoc % :selected (= p-id (-> % :p-id))))))

;; -------------------------------------------------------------------------------

(reg-sub
 ::db
 (fn [db [_ ks]]
   (get-in db ks 0)))

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
 (fn [db [_ transcript]]
   (assoc db
          ::words (into {}
                        (for [x (->> transcript (map :sentences) flatten
                                     (map :words) flatten)]
                          {(:id x) x}))
          ::transcript transcript)))

(reg-event-db
 ::text-fragment
 (fn [db [_ value]]
   (dispatch [::transcript (nlp/create-text-parts value)])
   (assoc db ::text-fragment value)))

(reg-sub
 ::words
 (fn [db [_ id]]
   (get-in db [::words])))

(reg-sub
 ::word
 (fn [db [_ id]]
   (get-in db [::words id])))

(reg-sub
 ::word-data
 (fn [db [_ id k]]
   (get-word-data db id k)))

(reg-event-db
 ::word-data
 (fn [db [_ id k v]]
   (set-word-data db id k v)))

(reg-event-db
 ::editor-on-layout
 (fn [db [_ id layout-data]]
   db))

(reg-sub
 ::select-in-progress
 (fn [db _]
   (get db ::select-in-progress false)))

(reg-sub
 ::scroll-pos
 (fn [db _]
   (get db ::scroll-pos 0)))

(reg-event-db
 ::scroll-pos
 (fn [db [_ value]]
   (assoc db ::scroll-pos value)))

(reg-event-db
 ::deselect
 (fn [db [_ id]]
   (deselect-all db)))

(reg-event-db
 ::word-click
 (fn [db [_ id gesture-state]]
   (-> db)))

(reg-event-db
 ::word-release
 (fn [db [_ id gesture-state]]
   (if (::select-in-progress db)
     (assoc db
            ::select-in-progress false
            ::prev-gesture-state nil
            ::prev-click nil
            ::count-click 0)
     (let [prev?       (= (::prev-click db) id)
           double?     (and prev?
                            (rn-utils/double-tap (::prev-gesture-state db) gesture-state))
           count-click (if double? (inc (::count-click db)) 1)]
         (case count-click
           1 (dispatch [::word-one-click id])
           2 (dispatch [::word-double-click id ])
           3 (dispatch [::word-triple-click id])
           "nothing")
         (assoc db
                ::select-in-progress false
                ::prev-gesture-state gesture-state
                ::prev-click id
                ::count-click count-click)))))

(reg-event-db
 ::word-one-click
 (fn [db [_ id]]
   (let [prev-selected (get-word-data db id :selected)]
     (-> db
         deselect-all
         (set-word-data id :selected (not prev-selected))))))

(reg-event-db
 ::word-double-click
 (fn [db [_ id]]
   (select-sentence db id)))

(reg-event-db
 ::word-triple-click
 (fn [db [_ id]]
   (select-paragraph db id)))

(reg-event-db
 ::select-data
 (fn [db [_ word-id gesture-state]]
   (let [first-word    (-> db ::words (get word-id))
         last-selected (calculate-collision  (::words db) gesture-state)]
     (if last-selected
       (-> db
           (assoc ::select-in-progress true)
           (select-words-range first-word last-selected))
       db))))

(reg-sub
 ::mode
 (fn [db _]
   #{:edit :search :record :idle}
   (get db ::mode :idle)))

(reg-event-db
 ::mode
 (fn [db [_ value]]
   (assoc db ::mode value)))

(reg-sub
 ::search-text
 (fn [db _]
   (get db ::search-text "")))

(reg-event-db
 ::search-text
 (fn [db [_ value]]
   (-> db
       (assoc ::search-text value)
       #_(filter-searched value))))

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
