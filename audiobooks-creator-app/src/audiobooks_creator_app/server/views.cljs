(ns audiobooks-creator-app.server.views
  (:require
   [reagent.core :as r :refer [atom]]
   [micro-rn.couchbase-lite :as cbl
    :refer [Server as-docs as-one-doc as-data-source aggregate <?
            get post delete put init view database document design-document attachment]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [audiobooks-creator-app.server.main :as server]
   [audiobooks-creator-app.server.utils :as u :refer [add-view]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(def users (-> views/users-design-document (view :users)))

(defn init-views []
  (let [port (chan)]
      (go
        (<! (add-view users-design-document
                      {:users
                       {:map "
                           function(doc){
                           if(doc.type == 'user'){emit(doc['email'], doc.name)}
                           }
                           "}

                       :user-balance
                       {:map "
                            function(doc){
                              if(doc.type == 'user.bet' &&  doc.archived == null){
                                emit(doc.user, (doc.profit || 0) - (doc.amount || 0));
                              }else if(doc.type == 'user.transaction'){
                                emit(doc.user, doc['balance-change']*1);
                              }
                           }"
                        :reduce "_sum"}}))
        (>! port "[init view complete]"))
      port))

(comment

 ;; ----- views

  (def sport-events-design-document (-> db (design-document :sport-events)))
  (def bets-design-document (-> db (design-document :bets)))
  (def users-design-document (-> db (design-document :users)))
  (def messages-design-document (-> db (design-document :messages)))

  (def current-events (-> sport-events-design-document (view :current-sport-events)))
  (def chemps (-> sport-events-design-document (view :chemps)))
  (def users-bets (-> bets-design-document (view :users-bets)))
  (def users-bets-by-events (-> bets-design-document (view :users-bets-by-events)))
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


  )
