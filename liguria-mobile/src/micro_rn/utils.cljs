(ns micro-rn.utils
  (:require
   [clojure.string :as string]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [clojure.walk :as walk]
   [goog.crypt.base64 :as base-64]
   [camel-snake-kebab.core :as keb :refer [->camelCase ->kebab-case ->kebab-case-keyword]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def react-ref (atom nil))

(defn get-react []
  (when-not @react-ref
    (reset! react-ref (js/require "react-native")))
  @react-ref)

(defn prepare-db-value [v]
  (str "\"" v "\""))

(defn transform-keys [m f]
  (let [f (fn [[k v]] [(f k) v])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn cemelify-keys [x]
  (some-> x (transform-keys ->camelCase) (transform-keys name)))

(defn keywordize [x]
  (some-> x (transform-keys keyword)))

(defn all-keys-camel-to-dash [x]
  (some-> x (transform-keys ->kebab-case-keyword)))

(defn prepare-to-clj [x]
  (some-> x js->clj keywordize all-keys-camel-to-dash))

(defn prepare-to-js [m]
  (clj->js (cemelify-keys m)))

(defn catch-err [fn]
  (try
    (fn)
    (catch js/Error e (js/console.error " -> " e))))

(defn- create-list-view-ds
  ([] (create-list-view-ds (fn [r1 r2] (not= r1 r2))))
  ([row-compare-fn]
   (let [DataSource (.-ListView.DataSource (get-react))]
     (DataSource. #js {:rowHasChanged row-compare-fn}))))

(defn vec->array [vec]
  (let [arr #js []]
    (doseq [x vec] (.push arr x))
    arr))

(defn- array->vec [arr]
  (loop [res []
         [v & t] arr]
    (if (not (nil? v))
      (recur (conj res v) t)
      res)))

(declare empty-ds)

(defn create-list-model
  ([model row-compare-fn]
   (if model
     (.cloneWithRows (create-list-view-ds row-compare-fn) (vec->array model))
     empty-ds))
  ([model]
   (if model
     (.cloneWithRows (create-list-view-ds) (vec->array model))
     empty-ds)))

(defn update-model
  [ds model]
  (.cloneWithRows ds (vec->array model)))

(def empty-ds (create-list-model []))

(defn re-quote [s]
  (let [special (set ".?*+^$[]\\(){}|")
        escfn #(if (special %) (str \\ %) %)]
    (apply str (map escfn s))))

(defn re-prepare [value]
  (str ".*"
       (-> value
           re-quote
           string/lower-case
           string/trim) ".*"))

(def lazy-id (atom 0))

(defn- clear-lazy []
  (js/clearInterval @lazy-id))

(defn lazy-call
  ([cb] (lazy-call cb 400))
  ([cb idle]
   (clear-lazy)
   (reset! lazy-id
           (js/setInterval
            (fn []
              (clear-lazy)
              (cb)) idle))))

(defn lazy-call-fn
  ([cb] #(lazy-call cb))
  ([cb idle] #(lazy-call cb idle)))

;; old conv 000

(def do-later-queue (atom []))
(def do-later-interval (atom -1))

(defn- do-next []
  (if-not (empty? @do-later-queue)
    (do
      (let [[[cb t] & tail] @do-later-queue]
        (println "do-next: " (fn? cb) (number? t) (count tail))
        (reset! do-later-queue tail)
        (reset! do-later-interval (js/setInterval do-next t))
        (catch-err cb)))
    (reset! do-later-interval -1)))

(defn do-later
  ([cb] (do-later cb 1))
  ([cb t]
   (reset! do-later-queue (concat @do-later-queue [[cb t]]))
   (when (neg? @do-later-interval) (do-next))))

;; -----

(defn conv
  ([cb] (conv cb 0))
  ([cb t]))

(defn db-key [k]
  {:key (prepare-db-value k)})

(defn- two-letters [ds]
  (.. (str "00" ds) (substr -2)))

(defn format-date [date]
  (let [date (if (or (string? date) (number? date)) (js/Date. date) date)]
    (str (.getUTCFullYear date) "-"
         (two-letters (inc (.getUTCMonth date))) "-"
         (two-letters (.getUTCDate date)))))

(defn format-time
  [date]
  (let [date (if (string? date) (js/Date. date) date)]
    (str (.getUTCFullYear date) "-"
         (two-letters (inc (.getUTCMonth date))) "-"
         (two-letters (.getUTCDate date)) " "
         (two-letters (.getHours date)) ":"
         (two-letters (.getMinutes date)))))

(defn format-money [n]
  (let [n (str n)]
    (if (-> n count (> 3))
      (let [[pre pro] (split-at (- (count n) 3) n)]
        (str (string/join "" pre) "," (string/join "" pro)))
      n)))

(defn get-cache-stamp
  ([] (get-cache-stamp (* 10 60 1000)))
  ([lifetime]
   (-> (js/Date.) (.getTime) (/ lifetime) (js/Math.round))))

(def set-timeout js/setTimeout)
(def clear-timeout js/clearTimeout)

(defn await
  [promise]
  (let [port (chan)]
    (-> promise
        (.then (fn [res] (put! port [nil res]))
               (fn [err] (put! port [err nil])))
        (.catch (fn [err] (put! port [err nil]))))
    port))

(defn await-cb [fnc & args]
  (let [port (chan)]
    (apply fnc (concat args [(fn [& a] (put! port (or a [])))]))
    port))

(defn await-cb! [fnc & args]
  (let [port (chan)]
    (try
      (apply fnc (concat args [(fn [& a] (put! port (or a [])))]))
      (catch js/Error e (put! port [])))
    port))

(defn pmap [f col]
  (let [chans (repeatedly (count col) chan)]
    (doseq [[c e] (map vector chans col)]
      (go (>! c (f e))))
    (map put! chans)))

(defn fetch
  ([url] (fetch "GET" url))
  ([method url] (fetch method url nil))
  ([method url body]
   (let [credintals  (when (re-find #".+:.+@" url) (-> url (string/split "//") (nth 1) (string/split "@") (nth 0)))
         auth-header (when credintals (str "Basic " (base-64/encodeString credintals)))
         headers     (merge (if body
                              {"Content-Type" "application/json"}
                              {"Accept" "application/json"})
                            (when credintals {"Authorization" auth-header}))]
     (fetch method url body headers)))
  ([method url body headers]
   (let [port     (chan)
         settings (merge {:method  method
                          :headers headers}
                         (when body {:body (-> body clj->js js/JSON.stringify)}))]
     (go
       (-> (js/fetch url (clj->js settings))
           (.then (fn [res] (.json res)))
           (.then (fn [res]
                    (put! port (let [res           (keywordize res)
                                     error-message (:error res)]
                                 (if error-message [res nil] [nil res])))))
           (.catch (fn [res] (put! port [(keywordize res) nil])))))
     port)))

(defn fetch-text
  [url]
  (let [port (chan)]
    (go
      (-> (js/fetch url)
          (.then (fn [res] (.text res)))
          (.then (fn [res]
                   (put! port [nil (keywordize res)]))
                 (fn [res]
                   (put! port [(keywordize res) nil])))
          (.catch (fn [res] (put! port [(keywordize res) nil])))))
    port))

(defn prepare-search-text [text]
  (-> (if-not (nil? text) text "")
      string/trim
      (string/replace #"_+" " ")
      (string/replace #"\s+" " ")
      (string/replace (re-pattern "[:?!,\\\".)(\\-]") "")
      string/lower-case))
