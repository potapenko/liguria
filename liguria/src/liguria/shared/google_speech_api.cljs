(ns liguria.shared.google-speech-api
  (:require [micro-rn.react-native :as rn]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]]
            [micro-rn.utils :as utils])
  (:require-macros
   [micro-rn.macros :refer [...]]
   [cljs.core.async.macros :refer [go go-loop]]))

(def google-speech (-> rn/NativeModules .-RNGoogleSpeech))

(def recognizing-results-chan (chan))
(def in-progress? (atom false))
(def subscribtion (atom nil))

(defn remove-punctuations-from-phrase [phrase]
  ;; TODO
  phrase)

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
                                  (when (:error result)
                                    (reset! in-progress? false))
                                  (println "result:" result)
                                  #_(when (:is-final result) (stop-recognizing))
                                  (put! recognizing-results-chan result))))))))

(defn start-recognizing [phrases]
  (println "start google speech recognizing")
  (reset! in-progress? true)
  (add-listener)
  (-> google-speech  (.startRecognizing
                      (->> phrases
                           (map remove-punctuations-from-phrase)
                           utils/prepare-to-js))))

(comment

  (start-recognizing ["привет как дела"])

  (stop-recognizing)

  (deref in-progress?)

  (-> google-speech .-startRecognizing)
  (-> google-speech .-stopRecognizing) (go-loop []
                                         (println (<! recognizing-results-chan))
                                         (recur)))
