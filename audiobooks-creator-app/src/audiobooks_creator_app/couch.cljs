(ns audiobooks-creator-app.couch
  (:require
   [reagent.core :as r :refer [atom]]
   [micro-rn.couchbase-lite :as cbl :refer [Server as-docs as-one-doc as-data-source aggregate <?
                                            get post delete put init view database document design-document attachment]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

;; ----- utils

(defn print-err [fn]
  (try
    (fn)
    (catch js/Error e (js/console.error "init db error:" e))))

(defn fake-result []
  (let [port (chan)]
    (>! port "[fake data]")
    port))

;; ----- server

(def server (Server.))
(def db-name "audiobooks-app")

;; 146.185.171.85
(def gateway-url (str "http://192.168.0.200:4984/" db-name))
(def gateway-url-pass (str "http://admin:flvbygfc@192.168.0.200:4984/" db-name))

(defn start-server []
  (println "init couchbase db")
  (-> server init))

(start-server) ;; DEBUG

(def db (-> server (database db-name)))

(defn add-doc [doc]
  (-> db (post doc) <?))

(defn get-doc [id]
  (-> db (document id) (get) <?))

(defn update-doc
  ([id new-data]
   (let [port (chan)]
     (go
       (let [exists (<! (get-doc id))
             document (-> db (document id))]
         (if exists
           (-> document (put {:rev (:_rev exists)} (into exists new-data))  <?)
           (console.warn "[update-doc] document not exists: " id)))
       (>! port "[update-doc complete]"))
     port))
  ([doc]
   (let [port (chan)]
     (go
       (let [id (:_id doc)
             exists (<! (get-doc id))
             document (-> db (document id))]
         (if exists
           (-> document (put {:rev (:_rev exists)} doc)  <?)
           (-> document (put doc)  <?)))
       (>! port "[update-doc complete]"))
     port)))

(defn delete-doc [doc]
  (-> db (document (:_id doc)) (delete {:rev (:_rev doc)}) <?))

(defn get-view-docs
  ([view result-atom] (get-view-docs view result-atom {}))
  ([view result-atom params]
   (go
     (reset! result-atom (<! (-> view (cbl/get params) (cbl/as-list) <?))))))

;; ----- views

(comment

  (def sports {:socker     {:name "футбол" :emoji "⚽" :id "socker" :image (js/require "./images/socker-icon.jpg")}
               :tennis     {:name "теннис" :emoji "🎾" :id "tennis" :image (js/require "./images/tennis-icon.jpg")}
               :basketball {:name "баскетбол" :emoji "🏀" :id "basketball" :image (js/require "./images/basketball-icon.jpg")}
               :hockey     {:name "хокей" :emoji "🏒" :id "hockey" :image (js/require "./images/hockey-icon2.jpg")}
               :baseball   {:name "бейсбол" :emoji "⚾" :id "baseball" :image (js/require "./images/baseball-icon3.jpg")}
               :football   {:name "ам. футбол" :emoji "🏈" :id "football" :image (js/require "./images/football-icon.jpg")}
               :rugby      {:name "регби" :emoji "🏉" :id "rugby" :image (js/require "./images/rugby-icon2.jpg")}})

 ;; local data

  (def user-balance (atom 0))
  (def lang (atom :ru))
  (def search-text (atom 0))
  (def init-complete (atom false))
  (def can-send-gifts (atom true))
  (def not-read-messages-count (atom 0))
  (def tab-index (atom 0))

 ;; user data

  (def current-user (atom nil))
  (def current-user-id (atom nil))
  (def facebook-id (atom nil))
  (def fb-token (atom nil))

 ;; ----- views

  (def sport-events-design-document (-> db (design-document :sport-events)))
  (def bets-design-document (-> db (design-document :bets)))
  (def users-design-document (-> db (design-document :users)))
  (def messages-design-document (-> db (design-document :messages)))

  (def current-events (-> sport-events-design-document (view :current-sport-events)))
  (def chemps (-> sport-events-design-document (view :chemps)))
  (def users-bets (-> bets-design-document (view :users-bets)))
  (def users-bets-by-events (-> bets-design-document (view :users-bets-by-events)))
  (def users (-> users-design-document (view :users)))
  (def user-balance-view (-> users-design-document (view :user-balance)))
  (def users-messages (-> messages-design-document (view :user-messages)))
  (def not-read-messages-count-view (-> messages-design-document (view :not-read-messages-count)))

 ;; ----- init

  (defn create-user-id [email]
    (str "user::" email))

  (defn update-balance []
    (let [port (chan)]
      (go
        (let [start-balance 100
              res           (<! (-> user-balance-view (cbl/get {:key @current-user-id}) (as-one-doc) <?))]
          (if res
            (reset! user-balance (-> res :value (+ start-balance)))
            (reset! user-balance start-balance)))
        (>! port @user-balance))
      port))

  (defn create-new-bet [event result]
    (let [port (chan)]
      (go
        (when (> @user-balance 1)
          (<! (add-doc {:type         "user.bet"
                        :user         @current-user-id
                        :channel      @facebook-id
                        :event-result result
                        :amount       1
                        :event        (:_id event)
                        :result       "wait"
                        :status       "wait"})))
        (<! (update-balance))
        (>! port "[create-new-bet complete]"))
      port))

  (defn update-not-read-messages-count []
    (let [port (chan)]
      (go
        (let [res (<! (-> not-read-messages-count-view (cbl/get {:key @current-user-id}) (as-one-doc) <?))]
          (if res
            (reset! not-read-messages-count (-> res :value))
            (reset! not-read-messages-count 0)))
        (>! port @not-read-messages-count))
      port))

  (defn update-last-gift-last-date [gifts-count]
    (update-doc {:_id     (str "gift-info::" (:email @current-user))
                 :type    "user.gifts-sent-info"
                 :user    @current-user-id
                 :channel @facebook-id
                 :count   gifts-count
                 :date    (str (js/Date.))}))

  (defn update-can-send-gifts []
    (let [port (chan)]
      (go
        (let [last-gift-info (<! (get-doc (str "gift-info::" (:email @current-user))))]
          (if last-gift-info
            (let [date-now     (js/Date.)
                  date-updated (js/Date. (:date last-gift-info))]
              (reset! can-send-gifts (not= (.getUTCDate date-now) (.getUTCDate date-updated))))
            (reset! can-send-gifts true))
          (>! port last-gift-info)))
      port))

  (defn send-gift-message [to-user-name to-user-fb-id]
    (add-doc
     (let [message-type (rand-nth ["friends" "result" "tournament"])]
       {:type         "user.message"
        :to           to-user-name
        :channel      to-user-fb-id
        :from         {:name (:name @current-user) :facebook-id @facebook-id}
        :sent         (js/Date.now)
        :already-read false
        :message-type "friends"
        :message-body {:type "gift" :amount 20 :message-text "Ваш друг подарил вам 20 монет."}})))

 ;; todo - move to server
  (defn new-user-transaction [type info value]
    (add-doc {:type             "user.transaction"
              :transaction-type "gift"
              :user             @current-user-id
              :channel          @facebook-id
              :info             info
              :balance-change   value}))

  (defn load-user [email]
    (let [port (chan)
          id   (create-user-id email)]
      (go
        (let [user (<! (get-doc id))]
          (reset! current-user user)
          (reset! current-user-id (:_id user))
          (<! (update-balance))
          (<! (update-can-send-gifts))
          (<! (update-not-read-messages-count))
          (>! port user)))
      port))

 ;; ----- init

  (defn init-user [first-name last-name email fb-data]
    (add-doc {:_id           (create-user-id email)
              :type          "user"
              :channel       (:id fb-data)
              :facebook-id   (:id fb-data)
              :email         email
              :locale        (:locale fb-data)
              :name          (str first-name " " last-name)
              :first-name    first-name
              :last-name     last-name
              :gender        (:gender fb-data)
              :facebook-data fb-data}))

  (defn add-view [design-doc views]
    (-> design-doc (put {:language "javascript", :views views}) <?))

  (defn init-views []
    (let [port (chan)]
      (go
        (<! (add-view sport-events-design-document
                      {:current-sport-events
                       {:map "
                           function(doc){
                           if(doc.type == 'sport-event' &&
                           doc.archived == null){emit([doc['chemp-id'], doc['sport-id']], doc.date)}
                           }
                           "}
                       :chemps
                       {:map    "
                            function(doc){
                            if(doc.type == 'sport-event' &&
                            doc.archived == null){emit([doc['chemp'], doc['chemp-id'], doc['sport-id']], 1)}
                            }
                            "
                        :reduce "_sum"}}))
        (<! (add-view users-design-document
                      {:users
                       {:map "
                           function(doc){
                           if(doc.type == 'user'){emit(doc['email'], doc.name)}
                           }
                           "}

                       :user-balance
                       {:map    "
                            function(doc){
                              if(doc.type == 'user.bet' &&  doc.archived == null){
                                emit(doc.user, (doc.profit || 0) - (doc.amount || 0));
                              }else if(doc.type == 'user.transaction'){
                                emit(doc.user, doc['balance-change']*1);
                              }
                           }
                           "
                        :reduce "_sum"}}))
        (<! (add-view bets-design-document
                      {:users-bets
                       {:map "
                           function(doc){
                           if(doc.type == 'user.bet' && !doc.archived){emit(doc['user'], doc.date)}
                           }
                           "}
                       :users-bets-by-events
                       {:map "
                           function(doc){
                           if(doc.type == 'user.bet' && !doc.archived){emit(doc['user'] + '@' + doc['event'], null)}
                           }
                           "}}))
        (<! (add-view messages-design-document
                      {:user-messages
                       {:map "
                           function(doc){
                           if(doc.type == 'user.message' &&
                           doc.archived == null){emit(doc['to'], doc['date'])}
                           }
                           "}
                       :not-read-messages-count
                       {:map    "
                            function(doc){
                            if(doc.type == 'user.message' &&
                            doc.archived == null && !doc['already-read']){emit(doc['to'], null)}
                            }
                           "
                        :reduce "_count"}}))

        (>! port "[init view complete]"))
      port))

 ;; ----- test data

  (defn random-sport []
    ((rand-nth (keys sports)) sports))

  (defn create-test-event [sport-config champ-name oponent-1 oponent-2]
    {:type          "sport-event"
     :sport-id      (:id sport-config)
     :sport-name    (string/capitalize (:name sport-config))
     :champ         champ-name
     :info          (str oponent-1 " - " oponent-2)
     :opponent-1    oponent-1
     :opponent-2    oponent-2
     :status        (rand-nth ["finished" "live" "today" "tomorow"])
     :result        {:info "1:1, ничья"}
     :date          (js/Date.)
     :avalible-bets [{:info (str oponent-1 " win") :coeff (+ 2 (rand-int 10)) :id 1}
                     {:info "tie" :coeff (+ 2 (rand-int 10)) :id 3}
                     {:info (str oponent-2 " win") :coeff (+ 2 (rand-int 10)) :id 2}]})

  (defn init-events []
    (let [port (chan)]
      (go
        (doseq [[k v] sports]
          (doseq [x (range 1 (+ 5 (rand-int 4)))]
            (let [champ-name (str "My champ " x)]
              (doseq [x (range 3 (+ 2 (rand-int 4)))]
                (<! (add-doc
                     (create-test-event v champ-name
                                        (rand-nth ["Спартак" "Зенит" "Барселона" "Мадрид"])
                                        (rand-nth ["Торпедо" "Металург" "Орландо" "Арсенал"]))))))))
        (>! port "[init events complete]"))

      port))

  (defn init-bets []
    (let [port (chan)]
      (go
        (doseq [x (<! (-> current-events (get) (as-docs) <?))]
          (let [event   @x
                results (:avalible-bets event)]
            (when (rand-nth [true false])
              (add-doc (let [status (rand-nth ["finished" "live" "wait"])
                             result (rand-nth ["wait" "win" "loose"])]
                         {:type         "user.bet"
                          :user         @current-user-id
                          :channel      @facebook-id
                          :event-result (rand-nth (take 3 results))
                          :date         (:date event)
                          :event        (:_id event)
                          :result       result
                          :profit       (if (and (= status "finished") (= result "win"))
                                          (inc (rand-int 30)) 0)
                          :amount       (inc (rand-int 10))
                          :result-nth   (rand-int 3)
                          :bet-nth      (rand-int 3)
                          :status       status})))))
        (>! port "[init bets complete]"))
      port))

  (defn init-gifts []
    (let [port (chan)]
      (go
        (doseq [x (range (inc (rand-int 50)))]
          (add-doc {:type             "user.transaction"
                    :transaction-type "gift"
                    :user             @current-user-id
                    :channel          @facebook-id
                    :info             {:from {:id          @current-user-id
                                              :facebook-id "1808059805877171"}}
                    :balance-change   (rand-int 100)}))
        (>! port ""))
      port))

  (defn init-messages []
    (println "init-messages")
    (let [port (chan)]
      (go
        (doseq [x (range 12)]
          (<! (add-doc (let [message-type (rand-nth ["friends" "result" "tournament"])]
                         {:type         "user.message"
                          :to           (:_id @current-user)
                          :channel      @facebook-id
                          :from         (case message-type "friends"
                                              {:id          @current-user-id
                                               :name        (:name @current-user)
                                               :facebook-id @facebook-id} "system")
                          :sent         (js/Date.now)
                          :already-read false
                          :message-type message-type
                          :message-body (case message-type
                                          "friends" {:type "gift" :amount 20 :message-text "Ваш друг подарил вам 20 очков."}
                                          {:bet {:profit     (str "+" (inc (rand-int 30)))
                                                 :result-nth (rand-int 3)
                                                 :bet-nth    (rand-int 3)
                                                 :event      (create-test-event (random-sport) ""
                                                                                (rand-nth ["Спартак" "Зенит" "Барселона" "Мадрид"])
                                                                                (rand-nth ["Торпедо" "Металург" "Орландо" "Арсенал"]))}})}))))
        (>! port "[users messages complete]"))
      port))

  (defn init-new-db []
    (let [port (chan)]
      (go
        (<! (-> db (delete) <?))
                                        ;(<! (-> db (put) <?))

        (>! port "(init-new-db) complete"))
      port))

  (defn fill-test-data []
    (let [port (chan)]
      (go
        (println (<! (init-events)))
        (println (<! (init-messages)))
        (println (<! (init-bets)))
        (println (<! (init-gifts)))

        (>! port "(fill-test-data) complete"))
      port))

  (defn delete-all-docs []
    (let [port (chan)]
      (go
        (doseq [x (:rows (<! (-> db (cbl/get-all-docs {}) <?)))]
          (let [d (<! (get-doc (:id x)))]
            (println "[delete doc]: " (:_id d) (<! (delete-doc d)))))
        (<! (init-new-db))
        (>! port "(delete-all-docs) complete"))
      port))

 ;; server start

  (defn init-server []
    (let [port (chan)]
      (go
        (println "start server:" (<! (start-server)))
        (let [all-dbs (<! (-> server (cbl/all-dbs) <?))]
          (when-not (contains? (set all-dbs) db-name)
            (println "[create-new-db] " (<! (-> db (put) <?)))
            (println (<! (init-views)))))
        (>! port "(init-server) complete"))
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

        (when-not (<! (load-user email))
          (<! (timeout 2000)))

        (>! port "(start-sync) complete"))

      port))

 ;; updater

  (go-loop []

    (when (and @current-user)
      ; (<! (update-balance))
      ; (<! (update-can-send-gifts))
      ; (<! (update-not-read-messages-count))
     )

    (<! (timeout (* 30 1000)))

    (recur)))
