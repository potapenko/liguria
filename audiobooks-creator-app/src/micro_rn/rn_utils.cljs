(ns micro-rn.rn-utils
  (:require [micro-rn.utils :as utils]))

(defn event->layout [e]
  (let [{:strs [x y width height]} (-> e .-nativeEvent .-layout js->clj)]
    {:w width :h height :x x :y y}))

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
