(ns micro-rn.rn-utils
  (:require [micro-rn.utils :as utils])
  (:require-macros [micro-rn.macros :refer [...]]))

(defn event->layout [e]
  (some-> e .-nativeEvent .-layout utils/prepare-to-clj))

(defn ref->layout [ref cb]
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
  (assoc
   (-> state utils/prepare-to-clj)
   :timestamp (js/Date.now)))

(defn ->gesture-props [responder]
  (some-> responder .-panHandlers js->clj))

(defn distance [x0 y0 x1 y1]
  (js/Math.sqrt
   (+ (js/Math.pow (- x1 x0) 2)
      (js/Math.pow (- y1 y0) 2))))

(defn double-tap [prev-gesture-state current-gesture-state]
  (when prev-gesture-state
    (let [dx        (distance (:x-0 prev-gesture-state)
                              (:y-0 prev-gesture-state)
                              (:x-0 current-gesture-state)
                              (:y-0 current-gesture-state))
          delay     (- (:timestamp current-gesture-state)
                       (:timestamp prev-gesture-state))
          radius    20
          max-delay 300]
      (and (< delay max-delay)
           (< dx radius)))))

(defn scroll-y [e]
  (-> e .-nativeEvent utils/prepare-to-clj :content-offset :y))

(defn gesture-state-distance [a b]
  (if-not (or (nil? a) (nil? b))
    (distance (:dx a) (:dy a) (:dx b) (:dy b))
    0))
