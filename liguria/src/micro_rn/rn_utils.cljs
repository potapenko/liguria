(ns micro-rn.rn-utils
  (:require [micro-rn.utils :as utils]
            [re-frame.loggers :as rf-log]
            [reagent.core :as r])
  (:require-macros [micro-rn.macros :refer [...]]))

(defn event->layout [e]
  (some-> e .-nativeEvent .-layout utils/prepare-to-clj))

(defn ref->layout [ref cb]
  (if ref
      (-> ref (.measure
                   (fn [x y width height page-x page-y]
                     (cb (... x y width height page-x page-y)))))
      (cb nil)))

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
          max-delay 400]
      (and (< delay max-delay)
           (< dx radius)))))

(defn scroll-y [e]
  (-> e .-nativeEvent utils/prepare-to-clj :content-offset :y))

(defn gesture-state-distance [a b]
  (if-not (or (nil? a) (nil? b))
    (distance (:dx a) (:dy a) (:dx b) (:dy b))
    0))

(defn layout-hit-test [layout gesture-state]
  (let [{:keys [move-x move-y]} gesture-state
        {:keys [width height page-x page-y]} layout
        left                                 page-x
        right                                (+ left width)
        top                                  (- page-y 0)
        bottom                               (+ top height)]
    (and (<= left move-x right)
         (<= top move-y bottom))))

(defn yellowbox [& ignored]
  (set! js/console.ignoredYellowBox
        (clj->js (vec ignored))))

(defn disable-overwriting-warnings []
  (def warn (js/console.warn.bind js/console))
  (rf-log/set-loggers!
   {:warn (fn [& args]
            (cond
              (= "re-frame: overwriting " (first args)) nil
              :else (apply warn args)))}))

(defn on-viewable-items-changed [item-fn]
  (fn [data]
    (let [change-log (->> data .-changed
                          (map (fn [e] [(-> e .-item .-id)
                                        (-> e .-isViewable)
                                        (-> e .-index)])))]
      (doseq [[id visible index] change-log]
        (item-fn id visible index)))))
