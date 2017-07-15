(ns audiobooks-creator-app.screens.recording.nlp
  (:require [clojure.string :as string]))

(def test-text "
В четверг четвертого числа в четыре с четвертью часа лигурийский регулировщик регулировал в Лигурии, но тридцать три корабля лавировали, лавировали, да так и не вылавировали, а потом протокол про протокол протоколом запротоколировал. Как интервьюером интервьюируемый лигурийский регулировщик речисто, да не чисто рапортовал, да не дорапортовал, дорапортовывал. Да так зарапортовался про размокропогодившуюся погоду, что дабы инцидент не стал претендентом на судебный прецедент, лигурийский регулировщик акклиматизировался в неконституционном Константинополе.

Где хохлатые хохотушки хохотом хохотали и кричали турке, который начерно обкурен трубкой: не кури, турка, трубку, купи лучше кипу пик, лучше пик кипу купи, а то придет бомбардир из Брандебурга, бомбами забомбардирует за то, что некто чернорылый у него полдвора рылом изрыл, вырыл и подрыл. Но на самом деле турка не был в деле. Да и Клара-краля в то время кралась к ларю, пока Карл у Клары кораллы крал, за что Клара у Карла украла кларнет. А потом на дворе деготниковой вдовы Варвары два этих вора дрова воровали. Но грех — не смех, не уложить в орех: о Кларе с Карлом во мраке все раки шумели в драке. Вот и не до бомбардира ворам было, но и не до деготниковой вдовы, и не до деготниковых детей. Зато рассердившаяся вдова убрала в сарай дрова: раз дрова, два дрова, три дрова — не вместились все дрова, и два дровосека, два дровокола-дроворуба для расчувствовавшейся Варвары выдворили дрова вширь двора обратно на дровяной двор, где цапля чахла, цапля сохла, цапля сдохла. Цыпленок же цапли цепко цеплялся за цепь. Молодец против овец, а против молодца - сам овца, которой носит Сеня сено в сани, потом везет Сенька Соньку с Санькой на санках: санки — скок, Сеньку — в бок, Соньку — в лоб, все — в сугроб.
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
      (string/replace #"\.\" " (str one-dot-mark "\"" sentence-separator))
      (string/replace #"!\" " (str one-exclamation-mark "\"" sentence-separator))
      (string/replace #"\?\" \" " (str one-question-mark "\"" sentence-separator))
      (string/replace #"\.' " (str one-dot-mark "'" sentence-separator))
      (string/replace #"!' " (str one-exclamation-mark "'" sentence-separator))
      (string/replace #"\?' " (str one-question-mark "'" sentence-separator));
      (string/replace #"\. " (str "." sentence-separator))
      (string/replace #"! " (str "!" sentence-separator))
      (string/replace #"\? " (str "?" sentence-separator));
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
  (-> test-text create-paragraphs first
      create-sentences #_count)
  (-> test-text create-paragraphs count))
