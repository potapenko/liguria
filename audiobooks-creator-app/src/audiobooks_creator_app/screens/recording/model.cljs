(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils]
            [clojure.string :as string]
            [audiobooks-creator-app.screens.recording.nlp :as nlp]
            [micro-rn.react-native :as rn]
            [clojure.walk :as walk]
            [micro-rn.utils :as utils])
  (:require-macros [micro-rn.macros :refer [...]]))

;; -------------------------------------------------------------------------------

(def test-db (atom nil))

(defn map-words [db f]
  (assoc db ::words
         (reduce-kv (fn [m k v]
                      (assoc m k (f v))) {} (::words db))))

(defn iterate-words [db k f]
  (doseq [x (-> db ::words vals)]
    (let [res (f x)]
      (when (not= res (k x))
          (dispatch [::word-data (:id x) k res])))))

(defn get-word-data [db id k]
  (get-in db [::words id k]))

(defn set-word-data [db id k v]
  (assoc-in db [::words id k] v))

(defn set-paragraph-data [db id k v]
  (assoc db ::transcript
         (->> db ::transcript (mapv #(if (= (:id %) id) (assoc % k v) %)))))

(defn get-paragraph-data [db id k]
  (some->> db ::transcript (filter #(= (:id %) id)) first k))

(defn set-sentence-data [db & id-k-v]
  (assoc db ::transcript
         (time (walk/postwalk (fn [x]
                           (loop [x            x
                                  [id k v & t] id-k-v]
                             (if id
                               (recur
                                (if (and (map? x)
                                         (= (:type x) :sentence)
                                         (= (:id x) id))
                                  (assoc x k v)
                                  x)
                                t)
                               x)))
                         (::transcript db)))))

(defn get-first [db]
  (->> db ::transcript (map :sentences) flatten first (#(dissoc % :words))))

(defn get-sentence-data [db id k]
  (some->> db ::transcript (map :sentences) flatten (filter #(= (:id %) id)) first k))

(defn select-words-range [db from to]
  (map-words db #(assoc % :selected
                        (or (<= (:id from) (:id %) (:id to))
                            (<= (:id to) (:id %) (:id from))))))

;; (defn filter-searched [db text]
;;   )

(defn get-words-line [db word-id]
  )

(defn get-sentence [db word-id]
  )

(defn get-paragraph [db word-id]
  )

(defn select-line [db id]
  (let [word-y (-> db ::words (get id) :layout :page-y)]
    (map-words db #(assoc % :selected (= word-y (-> % :layout :page-y))))))

(defn select-sentence [db id]
  (let [s-id (get-word-data db id :s-id)]
    (iterate-words db :selected #(= s-id (-> % :s-id)))
    db))

(defn select-paragraph [db id]
  (let [p-id (get-word-data db id :p-id)]
    (iterate-words db :selected #(= p-id (-> % :p-id)))
    db))

(defn select-all [db]
  (iterate-words db :selected #(do true))
  db)

(defn deselect-all [db]
  (iterate-words db :selected #(do false))
  db)

(defn visible-paragraph? [p]
  (let [v (:visible p)]
    (if (nil? v) true v)))

(defn get-visible-words [db]
  (let [words-ids (some->> db ::transcript
                           (filter #(visible-paragraph? %))
                           (map :sentences) flatten
                           (map :words) flatten
                           (map :id))]
    (for [id words-ids]
      (get (::words db) id))))

(defn calculate-collision [db gesture-state]
  (let [words                   (::words db)
        {:keys [move-x move-y]} gesture-state
        scroll-diff                     (::scroll-pos db)]
    (->> (get-visible-words db)
         (filter (fn [w]
                   (let [{:keys [width height page-x page-y]} (:layout w)
                         left   page-x
                         right  (+ left width)
                         top    (- page-y 0 #_scroll-diff)
                         bottom (+ top height)]
                     (and (<= left move-x right)
                          (<= top move-y bottom)))))
         first)))

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

(reg-event-db
 ::paragraph-visible
 (fn [db [_ id value]]
   (set-paragraph-data db id :visible value)))

(reg-sub
 ::paragraph-visible
 (fn [db [_ id]]
   (let [v (get-paragraph-data db id :visible)]
     (if (nil? v) true v))))

(reg-event-db
 ::sentence-visible
 (fn [db [_ id value]]
   (set-sentence-data db id :visible value)))

(reg-sub
 ::sentence-visible
 (fn [db [_ id]]
   (let [v (get-sentence-data db id :visible)]
     (if (nil? v) true v))))

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

(reg-sub
 ::words-ids
 (fn [db _]
   (-> db ::words keys)))

(reg-event-db
 ::scroll-pos
 (fn [db [_ value]]
   #_(dispatch [::update-words-layouts])
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
           2 (select-sentence db id)
           3 (select-paragraph db id)
           ;; 4 (select-all db)
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
 ::find-collision-and-select-word-range
 (fn [db [_ first-word words gesture-state]]
   (when (= (::prev-gesture-state db) gesture-state)
     (let [[cur-word & tail] words]
       (when cur-word
         (println "select in progress" (count words))
         (let [next-word #(dispatch [::find-collision-and-select-word-range
                                     first-word tail gesture-state])
               ref       (:ref cur-word)]
           (if-not (nil? ref)
             (rn-utils/ref->layout
              ref
              #(if (rn-utils/layout-hit-test % gesture-state)
                 (select-words-range db first-word cur-word)
                 (next-word)))
             (next-word))))))
   db))

(reg-event-db
 ::prev-gesture-state
 (fn [db [_ value]]
   (assoc db ::prev-gesture-state value)))

(reg-event-db
 ::update-words-layouts
 (fn [db [_ value]]
   #_(doseq [x (get-visible-words db)]
     (let [{:keys [id ref]} x]
       (rn-utils/ref->layout ref #(dispatch [::word-data id :layout %]))))
   db))

(reg-event-db
 ::select-progress
 (fn [db [_ word-id gesture-state]]
   db
   #_(let [in-progress (::select-in-progress db)
           distance (rn-utils/gesture-state-distance (::prev-gesture-state db) gesture-state)]
       (if (or (not in-progress) (> distance 10))
         (let [first-word    (-> db ::words (get word-id))
               last-selected (calculate-collision db gesture-state)]
           (-> db
               (assoc ::select-in-progress true
                      ::prev-gesture-state gesture-state)
               (#(if last-selected (select-words-range % first-word last-selected) %))))
         db))))

(reg-event-db
 ::select-progress-new
 (fn [db [_ word-id gesture-state]]
   (let [in-progress (::select-in-progress db)
         distance (rn-utils/gesture-state-distance (::prev-gesture-state db) gesture-state)]
     (if (or (not in-progress) (> distance 10))
       (let [first-word    (-> db ::words (get word-id))]
         (dispatch [::prev-gesture-state gesture-state])
         #_(dispatch [::find-collision-and-select-word-range
                    first-word (get-visible-words db) gesture-state])
         (assoc db ::select-in-progress true)
       db)))))

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
 (fn [db [_ text]]
   (time (if text
      (let [rx (re-pattern (str (string/lower-case (or text "")) ".+"))]
        (loop [db      (assoc db ::search-text text)
               [x & t] (->> db ::transcript (map :sentences) flatten)]
          (if x
            (recur
             (let [res (->>  x :text string/lower-case (re-find rx) nil? not)]
               (set-sentence-data db (:id x) :visible res))
             t)
            (do
              (reset! test-db db)
              db)
            )))
      db))))

;; (get-sentence-data @test-db )

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
