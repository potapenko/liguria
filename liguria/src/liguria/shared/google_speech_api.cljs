(ns liguria.shared.google-speech-api
  (:require [micro-rn.react-native :as rn]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [liguria.shared.nlp :as nlp])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(def google-speech (-> rn/NativeModules .-RNGoogleSpeech))

(def recognizing-results-chan (chan))
(def in-progress? (atom false))
(def subscribtion (atom nil))

(defn remove-punctuation-from-phrases [phrases]
  (if (or (nil? phrases) (empty? phrases))
    nil
    (->> phrases
         (map (fn [e]
                (-> e
                    string/lower-case
                    (string/replace #"[.,\/#!$%\^&\*;:{}=\-_`~()—]" "")
                    (string/replace #"\s+" " ")
                    string/trim))
              )
         vec)))

(defn stop-recognizing []
  (println "stop google speech recognizing")
  (reset! in-progress? false)
  (-> google-speech .stopRecognizing))

(defn add-listener []
  (when-not @subscribtion
    (reset! subscribtion
            (-> rn/NativeAppEventEmitter
                (.addListener "GoogleRecognizeResult"
                              (fn [result]
                                (let [result (utils/prepare-to-clj result)]
                                  (when @in-progress?
                                    (println "result:" result)
                                    (when (:error result)
                                      (reset! in-progress? false))
                                    (when (:is-final result)
                                      (stop-recognizing))
                                    (put! recognizing-results-chan result)))))))))

(defn start-recognizing [& phrases]
  (let [phrases (->> phrases
                    remove-punctuation-from-phrases
                    utils/prepare-to-js)]
    (println "start google speech recognizing:" phrases)
    (reset! in-progress? true)
    (add-listener)
    (-> google-speech  (.startRecognizing phrases))))

(comment

  (start-recognizing
   "В четверг четвертого числа в четыре с четвертью часа лигурийский регулировщик регулировал в Лигурии")

  (start-recognizing "один" "два" "три")

  (start-recognizing)

  (deref in-progress?)

  (-> google-speech .-startRecognizing)
  (-> google-speech .-stopRecognizing)
  (go-loop []
    (println (<! recognizing-results-chan))
    (recur)))
