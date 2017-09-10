(ns liguria.shared.nlp
  (:require [clojure.string :as string]))

(def test-text "
")

(def one-dot-mark "#one#")
(def one-question-mark "#question#")
(def one-exclamation-mark "#exclamation#")
(def sentence-separator "#sentence#")
(def escape_dot "#escape_dot#")
(def escape_qustion "#escape_question#")
(def escape_exclamation "#escape_exclamation#")

(defn create-words [s]
  (string/split s #"\s+"))

(defn- person-dots [s]
  (let [person-words ["Dr" "Prof" "Ms" "Mrs" "Mr" "St"]]
    (loop [[v t] person-words
           s s]
      (if v
        (recur t (string/replace s (str v ".") (str v one-dot-mark)))
        s))))

(def create-sentences
  (memoize
   (fn [s]
     (-> s
         person-dots
         (->
          (string/replace #"\.\*" escape_dot)
          (string/replace #"\?\*" escape_qustion)
          (string/replace #"\!\*" escape_exclamation))
         (->
          (string/replace #"\.\"\s+" (str one-dot-mark "\"" sentence-separator))
          (string/replace #"!\"\s+" (str one-exclamation-mark "\"" sentence-separator))
          (string/replace #"\?\"\s+\" " (str one-question-mark "\"" sentence-separator))
          (string/replace #"\.'\s+" (str one-dot-mark "'" sentence-separator))
          (string/replace #"!'\s+" (str one-exclamation-mark "'" sentence-separator))
          (string/replace #"\?'\s+" (str one-question-mark "'" sentence-separator)) ;
          (string/replace #"\.\s+" (str "." sentence-separator))
          (string/replace #"!\s+" (str "!" sentence-separator))
          (string/replace #"\?\s+" (str "?" sentence-separator)) ;
          (string/replace one-dot-mark ".")
          (string/replace one-question-mark "?")
          (string/replace one-exclamation-mark "!")
          (string/split sentence-separator))
         (->>
          (map #(string/replace % escape_dot "."))
          (map #(string/replace % escape_qustion "?"))
          (map #(string/replace % escape_exclamation "!")))))))

(create-sentences "")

(comment

  (create-sentences " .\" ")

  (-> test-text create-paragraphs first
      create-sentences count))

(def create-paragraphs
  (memoize
   (fn [s]
     (->> s
          (#(string/split % #"(\r|\n)+"))
          (map string/trim)
          (filter (complement empty?))))))

(defn create-text-parts [source]
  (let [p-counter  (atom 0)
        w-counter  (atom 0)
        s-counter  (atom 0)
        paragraphs (cond
                     (string? source) (time (create-paragraphs source))
                     (seq? source)    source)]
    (println "create paragraphs done")
    (for [p paragraphs]
      (do
        (println "id:" @p-counter)
        {:type      :paragraph
         :id        (swap! p-counter inc)
         :text      p
         :sentences (for [s (time (create-sentences p))]
                      {:type  :sentence
                       :id    (swap! s-counter inc)
                       :p-id  @p-counter
                       :text  s
                       :words (for [w (create-words s)]
                                {:type :word
                                 :id   (swap! w-counter inc)
                                 :p-id @p-counter
                                 :s-id @s-counter
                                 :text w})})}))))

(comment
  (-> test-text create-text-parts count)
  (-> test-text create-text-parts first println)
  (-> test-text create-paragraphs first
      create-sentences #_count)
  (-> test-text create-paragraphs count))
