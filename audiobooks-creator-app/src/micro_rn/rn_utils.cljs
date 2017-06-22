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

(defn distance [x0 y0 x1 y1]
  (js/Math.sqrt
   (+ (js/Math.pow (- x1 x0) 2)
      (js/Math.pow (- y1 y0) 2))))

(defn double-tap [prev-gesture-state current-gesture-state]
  (let [distance  (distance (:x0 prev-gesture-state)
                            (:y0 prev-gesture-state)
                            (:x0 current-gesture-state)
                            (:y0 current-gesture-state))
        delay     (- (:timestamp prev-gesture-state)
                     (:timestamp current-gesture-state))
        radius    20
        max-delay 300]
    (and (< delay max-delay)
         (< distance radius))))
