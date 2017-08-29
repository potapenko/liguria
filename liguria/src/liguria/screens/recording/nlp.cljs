(ns liguria.screens.recording.nlp
  (:require [clojure.string :as string]))

(def test-text "
")


(def one-dot-mark         "#one#")
(def one-question-mark    "#question#")
(def one-exclamation-mark "#exclamation#")
(def sentence-separator     "#sentence#")

(defn create-words [s]
  (string/split s #"\s+"))

(defn- person-dots [s]
  (let [person-words ["Dr" "Prof" "Ms" "Mrs" "Mr" "St"]]
    (loop [[v t] person-words
           s s]
      (if v
        (recur t (string/replace s (str v ".") (str v one-dot-mark)))
        s))))

(defn create-sentences [s]
  (-> s
      person-dots
      (string/replace #"\.\"\s+" (str one-dot-mark "\"" sentence-separator))
      (string/replace #"!\"\s+" (str one-exclamation-mark "\"" sentence-separator))
      (string/replace #"\?\"\s+\" " (str one-question-mark "\"" sentence-separator))
      (string/replace #"\.'\s+" (str one-dot-mark "'" sentence-separator))
      (string/replace #"!'\s+" (str one-exclamation-mark "'" sentence-separator))
      (string/replace #"\?'\s+" (str one-question-mark "'" sentence-separator));
      (string/replace #"\.\s+" (str "." sentence-separator))
      (string/replace #"!\s+" (str "!" sentence-separator))
      (string/replace #"\?\s+" (str "?" sentence-separator));
      (string/replace one-dot-mark ".")
      (string/replace one-question-mark "?")
      (string/replace one-exclamation-mark "!")
      (string/split sentence-separator)))

(comment

  (create-sentences " .\" ")

  (-> test-text create-paragraphs first
      create-sentences count))

(defn create-paragraphs [s]
  (->> s
       (#(string/split % #"(\r|\n)+"))
       (map string/trim)
       (filter #(-> % empty? not))))

(defn create-text-parts [source]
  (let [p-counter  (atom 0)
        w-counter  (atom 0)
        s-counter  (atom 0)
        paragraphs (cond
                     (string? source) (create-paragraphs source)
                     (seq? source)    source)]
    (doall
     (for [p paragraphs]
       {:type      :paragraph
        :id        (swap! p-counter inc)
        :text      p
        :sentences (doall
                    (for [s (create-sentences p)]
                      {:type  :sentence
                       :id    (swap! s-counter inc)
                       :p-id  @p-counter
                       :text  s
                       :words (doall
                               (for [w (create-words s)]
                                 {:type :word
                                  :id   (swap! w-counter inc)
                                  :p-id @p-counter
                                  :s-id @s-counter
                                  :text w}))}))}))))

(comment
  (-> test-text create-text-parts count)
  (-> test-text create-text-parts first println)
  (-> test-text create-paragraphs first
      create-sentences #_count)
  (-> test-text create-paragraphs count))
