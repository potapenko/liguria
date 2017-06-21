(ns micro-rn.rn-utils
  (:require [micro-rn.utils :as utils])
  (:require-macros [micro-rn.macros :refer [...]]))

(defn event->layout [e]
  (some-> e .-nativeEvent .-layout utils/prepare-to-clj))

(defn event->layout-ref [ref cb]
  (some-> ref (.measure
               (fn [x y width height page-x page-y]
                 (cb (... x y width height page-x page-y))))))

(defn event->pan-data [e]
  (let [e (-> e .-nativeEvent)]
    {:identifier (-> e .-identifier)
     :location-x (-> e .-locationX)
     :location-y (-> e .-locationY)
     :page-x     (-> e .-pageX)
     :page-y     (-> e .-pageY)
     :target     (-> e .-target)
     :timestamp  (-> e .-timestamp)}))

(defn ->getsture-state [state]
  (-> state utils/prepare-to-clj))

(defn ->gesture-props [responder]
  (some-> responder .-panHandlers js->clj))
