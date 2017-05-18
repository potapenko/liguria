(ns micro-rn.facebook-api
  (:require
   [reagent.core :as r :refer [atom]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]])

  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

  ;; FBSDK

(def login-button (r/adapt-react-class (aget (js/require "react-native-fbsdk") "LoginButton")))

(def LoginManager (aget (js/require "react-native-fbsdk") "LoginManager"))
(def ShareDialog (aget (js/require "react-native-fbsdk") "ShareDialog"))
(def ShareApi (aget (js/require "react-native-fbsdk") "ShareApi"))
(def GraphRequest (aget (js/require "react-native-fbsdk") "GraphRequest"))
(def GraphRequestManager (aget (js/require "react-native-fbsdk") "GraphRequestManager"))
(def AccessToken (aget (js/require "react-native-fbsdk") "AccessToken"))

(defn get-access-token []
  (let [port (chan)]
    (-> AccessToken
        (.getCurrentAccessToken)
        (.then
         (fn [token]
           (put! port {:access-token   (some-> token .-accessToken)
                       :user-id        (some-> token .-userID)
                       :application-id (some-> token .-applicationID)}))))
    port))

(def graph-queue (atom (chan)))
(def waiting-queue (atom (chan)))
(def cache (atom {}))

(go-loop []
  (let [cb (<! @graph-queue)]
    (cb)
    (<! @waiting-queue)
    (recur)))

(defn get-graph-data
  ([url] (get-graph-data url nil))
  ([url fields]
   (let [port (chan)
         lifetime-stamp (utils/get-cache-stamp)]
     (put! @graph-queue
           (fn []
             (if-let [exists (get @cache [lifetime-stamp url fields])]
               (do
                 (put! @waiting-queue "ping")
                 (put! port exists))
               (-> (GraphRequestManager.)
                   (.addRequest
                    (GraphRequest.
                     url
                     (if (nil? fields) fields (clj->js {:parameters {:fields {:string fields}}}))
                     (fn [err res]
                       (let [result (utils/keywordize [err res])]
                         (when-not err (reset! cache (assoc @cache [lifetime-stamp url fields] result)))
                         (put! @waiting-queue "ping")
                         (put! port result)))))
                   (.start)))))
     port)))

(defn get-fb-user  []
  (<! (get-graph-data "/me" "picture.type(large),about,first_name,last_name,email,locale,gender,birthday")))

(defn login
  ([] (login ["public_profile" "email" "user_friends"]))
  ([permissions]
   (let [port (chan)]
     (go
       (let [[err res] (<! (utils/await (-> LoginManager (.logInWithReadPermissions permissions))))]
         (if (or (not (nil? err)) (nil? res) (.-isCancelled res))
           (>! port false)
           (>! port true))))
     port)))
