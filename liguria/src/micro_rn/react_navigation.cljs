(ns micro-rn.react-navigation
  (:refer-clojure :exclude [pop!])
  (:require [reagent.core :as r]
            [micro-rn.react-native :as rn]
            [camel-snake-kebab.core :refer [->camelCase]]
            [micro-rn.utils :as utils])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn check-value [func]
  (fn [[key val]]
    (if (map? val)
      [(->camelCase key) (func val)]
      [(->camelCase key) val])))

(defn transform-params [params]
  (into {} (map (check-value transform-params) params)))

(def Navigation (js/require "react-navigation"))
(def StackNavigator (. Navigation -StackNavigator))
(def TabNavigator (. Navigation -TabNavigator))

(defn create-stack-navigator
  ([params]
   (create-stack-navigator params {}))
  ([params stack-config]
   (StackNavigator
    (clj->js (transform-params params))
    (clj->js (transform-params stack-config)))))

(defn create-tab-navigator
  ([params]
   (create-tab-navigator params {}))
  ([params tab-config]
   (TabNavigator
    (clj->js (transform-params params))
    (clj->js (transform-params tab-config)))))

(defn get-screen-wrapper [child]
  (fn screen-wrapper [props]
    [child props]))

(defn props->params [props]
  (-> props :navigation utils/prepare-to-clj :state :params))

(defn props->navigator [props]
  (-> props .-navigation))

(defn create-screen
  ([content]
   (create-screen {} content))
  ([params content]
   (let [screen (r/reactify-component
                 (get-screen-wrapper
                  (if (vector? content)  ;; check if that reagent component
                    (fn [props] content) ;; or just hiccup
                    content)))]
     (set! (.-navigationOptions screen) (clj->js (transform-params params)))
     screen)))


(defn navigate!
  [navigator screen props]
  (. navigator
     (navigate
      (-> screen (->camelCase) (name))
      (clj->js props))))

(defn go-back! [navigator]
  (. navigator (goBack)))

(defn import-module [name]
  (-> (js/require "react-navigation") (aget name) (rn/adapt-react-class "react-navigation")))

