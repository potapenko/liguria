(ns audiobooks-creator-app.screens.recording.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync subscribe]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils]
            [clojure.string :as string]
            [audiobooks-creator-app.screens.recording.nlp :as nlp]
            [micro-rn.react-native :as rn]
            [clojure.walk :as walk]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [natal-shell.interaction-manager :as interaction-manager]))

;; -------------------------------------------------------------------------------

(def test-db (atom nil))

(defn map-words [db f]
  (assoc db ::transcript
         (for [p (::transcript db)]
           (assoc p :sentences
                  (for [s (:sentences p)]
                    (assoc s :words
                           (for [w (:words s)]
                             (f w))))))))

(defn get-word [db id]
  (some->> db ::transcript (map :sentences) flatten (filter #(= (:id %) id)) first))

(defn get-word-data [db id k]
  (get (get-word db id) k))

(defn set-word-data [db id k v]
  (let [{:keys [s-id p-id]} (get-word db id)]
    (assoc db ::transcript
           (for [p (::transcript db)]
             (if (= p-id (:p-id p))
               (assoc p :sentences
                      (for [s (:sentences p)]
                        (if (= (:id s) s-id)
                          (assoc s :words
                                 (for [w (:words s)]
                                   (if (= id (:id w))
                                     (assoc w k v)
                                     w)))
                          s)))
               p)))))

(defn set-paragraph-data [db id k v]
  (assoc db ::transcript
         (->> db ::transcript (mapv #(if (= (:id %) id) (assoc % k v) %)))))

(defn get-paragraph [db id]
  (some->> db ::transcript (filter #(= (:id %) id)) first))

(defn get-paragraph-data [db id k]
  (get (get-paragraph db id) k))

(defn get-sentence [db id]
  (some->> db ::transcript (map :sentences) flatten (filter #(= (:id %) id)) first))

(defn get-sentence-data [db id k]
  (some->> db ::transcript (map :sentences) flatten (filter #(= (:id %) id)) first k))

(defn set-sentence-data [db id k v]
  (let [p-id (get-sentence-data db id :p-id)]
    (assoc db ::transcript
           (for [p (::transcript db)]
             (if (= p-id (:p-id p))
                 (assoc p :sentences
                        (for [x (:sentences p)]
                          (if (= (:id x) id)
                            (assoc x k v)
                            x)
                          ))
                 p)))))

(defn get-first [db]
  (->> db ::transcript (map :sentences) flatten first (#(dissoc % :words))))

(defn select-words-range [db from to]
  (map-words db #(assoc % :selected
                        (or (<= (:id from) (:id %) (:id to))
                            (<= (:id to) (:id %) (:id from))))))

(defn select-line [db id]
  (let [word-y (-> (get-word-data db id :layout) :page-y)]
    (map-words db #(assoc % :selected (= word-y (-> % :layout :page-y))))))

(defn select-sentence [db id]
  (let [s-id (get-word-data db id :s-id)]
    (map-words db #(assoc % :selected (= s-id (-> % :s-id))))))

(defn select-paragraph [db id]
  (let [p-id (get-word-data db id :p-id)]
    (map-words db #(assoc % :selected (= p-id (-> % :p-id))))))

(defn select-all [db]
  (map-words db #(assoc % :selected true)))

(defn deselect-all [db]
  (map-words db #(assoc % :selected false)))

(defn get-visible-words [db]
  (let [words-ids (some->> db ::transcript
                           (filter #(-> % :hidden not))
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

(defn get-paragraph-y [id]
  (loop [id (dec id)
         y  0]
    (if (pos? id)
      (recur (dec id)
             (+ y (:height @(subscribe [::paragraph-data id :layout]))))
      y)))

(defn scroll-to-sentence [id]
  (go
    (<! (utils/await-cb rn/run-after-interactions))
    (<! (utils/await-cb rn/request-animation-frame))
    (let [list-ref @(subscribe [::list-ref])]
      (if (= id 1)
        (-> list-ref (.scrollToOffset 0))
        (let [list-layout    @(subscribe [::list-layout])
              s-y            (:y @(subscribe [::sentence-data id :layout]))
              p-id           @(subscribe [::sentence-data id :p-id])
              p-y            (get-paragraph-y p-id)
              new-scroll-pos (+ p-y s-y)]
          (println (...  p-y s-y new-scroll-pos))
          (-> list-ref (.scrollToOffset (clj->js {:offset new-scroll-pos}))))))))

(comment
  @(subscribe [::sentence-data 17 :text])
  @(subscribe [::scroll-pos])
  (get-paragraph-y 3)
  (scroll-to-sentence 1)
  (scroll-to-sentence 17)
  (dispatch [::sentence-click 17])
  )

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
   (assoc db
          ::mode (if value :record :idle)
          ::monitoring 0
          ::recording value)))

(reg-sub
 ::transcript
 (fn [db _]
   (get db ::transcript [])))

(reg-event-db
 ::transcript
 (fn [db [_ transcript]]
   (assoc db
          ::transcript transcript)))

(reg-event-db
 ::text-fragment
 (fn [db [_ value]]
   (dispatch [::transcript (nlp/create-text-parts value)])
   (assoc db ::text-fragment value)))

(reg-event-db
 ::paragraph-hidden
 (fn [db [_ id value]]
   (set-paragraph-data db id :hidden value)))

(reg-sub
 ::paragraph-data
 (fn [db [_ id k]]
   (get-paragraph-data db id k)))

(reg-event-db
 ::paragraph-data
 (fn [db [_ id k value]]
   (set-paragraph-data db id k value)))

(reg-event-db
 ::paragraph-click
 (fn [db [_ id value]]
   (println "paragraph click:" id)
   (dispatch [::deselect])
   db))

(reg-event-db
 ::sentence-click
 (fn [db [_ id value]]
   (println "sentence click:" id)
   (if (= (::mode db) :search)
     (do
       (if true #_(and
            (-> db ::search-text string/blank? not)
            (not= id 1))
         (dispatch [::scroll-and-select id])
         (dispatch [::mode :idle]))
       db)
     (do
       (dispatch [::deselect])
       db))))

(reg-sub
 ::sentence-data
 (fn [db [_ id k]]
   (get-sentence-data db id k)))

(reg-event-db
 ::sentence-data
 (fn [db [_ id k value]]
   (set-sentence-data db id k value)))

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
   #_(dispatch [::update-words-layouts])
   (assoc db ::scroll-pos value)))

(reg-event-db
 ::deselect
 (fn [db [_ id]]
   (deselect-all db)))

(reg-event-db
 ::word-click
 (fn [db [_ id]]
   db))

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
 ::scroll-to-sentence
 (fn [db [_ id]]
   (scroll-to-sentence id)
   db))

(reg-event-db
 ::scroll-and-select
 (fn [db [_ id]]
   (dispatch [::mode :idle])
   (dispatch [::scroll-to-sentence id])
   db))

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
   (merge db
          {::mode value}
          (when (= value :idle) {::search-text ""}))))

(reg-event-db
 ::list-ref
 (fn [db [_ value]]
   (assoc db ::list-ref value)))

(reg-sub
 ::list-ref
 (fn [db _]
   (get db ::list-ref)))

(reg-event-db
 ::list-layout
 (fn [db [_ value]]
   (assoc db ::list-layout value)))

(reg-sub
 ::list-layout
 (fn [db _]
   (get db ::list-layout)))

(reg-sub
 ::search-text
 (fn [db _]
   (get db ::search-text "")))

(reg-event-db
 ::search-text
 (fn [db [_ text]]
   (assoc db ::search-text text)))

(reg-sub
 ::text-size
 (fn [db _]
   (get db ::text-size 16)))

(reg-event-db
 ::text-size
 (fn [db [_ value]]
   (assoc db ::text-size value)))

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

