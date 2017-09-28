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

(defn remove-punctuations-from-phrase [phrase]
  ;; TODO
  phrase)

(defn start-recognizing [phrases-array]
  (reset! in-progress? true)
  (-> google-speech  (.startRecognizing
                      (->> phrases-array
                           (map remove-punctuations-from-phrase)
                           utils/prepare-to-js)
                      (fn [result]
                        (let [result (utils/prepare-to-clj result)]
                          (when (:error result)
                            (reset! in-progress? false))
                          (put! recognizing-results-chan result))))))

(defn stop-recognizing []
  (reset! in-progress? false)
  (-> google-speech .stopRecognizing))


(comment
  (start-recognizing nil)

  (start-recognizing ["привет как дела"])

  (deref in-progress?)

  (go-loop []
    (println (<! recognizing-results-chan)))

  )
