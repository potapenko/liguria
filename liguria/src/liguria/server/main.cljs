(ns liguria.server.main
  (:require
   [reagent.core :as r :refer [atom]]
   [micro-rn.couchbase-lite :as cbl :refer [Server init database <? put get delete]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; ----- server

(def server (Server.))
(def db-name "audiobooks-app")

(def gateway-url (str "http://192.168.0.200:4984/" db-name))
(def gateway-url-pass (str "http://admin:flvbygfc@192.168.0.200:4984/" db-name))

(defn start-server []
  (println "init couchbase db")
  (-> server init))

(start-server) ;; DEBUG

(def db (-> server (database db-name)))

(defn init-server []
  (let [port (chan)]
    (go
      (println "start server:" (<! (start-server)))
      (let [all-dbs (<! (-> server (cbl/all-dbs) <?))]
        (when-not (contains? (set all-dbs) db-name)
          (println "[create-new-db] " (<! (-> db (put) <?)))
          ;; TODO переделать на mount
          #_(println (<! (init-views)))))
      (>! port "(init-server) complete"))
    port))

(defn init-new-db []
  (let [port (chan)]
    (go
      (<! (-> db (delete) <?))
      #_(<! (-> db (put) <?))
      (>! port "(init-new-db) complete"))
    port))

(defn start-sync [access-token email]
  (let [port (chan)]
    (go
      (println "[session before] " (<! (-> server (cbl/session) <?)))

      (println "[auth] "
               (<! (-> server
                       (cbl/authentication)
                       (cbl/facebook-token
                        {:access_token access-token
                         :email        email
                         :remote_url   gateway-url}) <?)))

      (println "[session after] " (<! (-> server (cbl/session) <?)))

      (println (<! (-> server (cbl/replicate {:target     db-name
                                              :source     gateway-url
                                              :continuous true}) <?)))

      (println (<! (-> server (cbl/replicate {:target     gateway-url
                                              :source     db-name
                                              :continuous true}) <?)))

      ;; TODO переделать на mount
      #_(when-not (<! (load-user email))
          (<! (timeout 2000)))

      (>! port "(start-sync) complete"))

    port))
