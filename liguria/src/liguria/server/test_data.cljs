(ns liguria.server.test-data
  (:require
   [reagent.core :as r :refer [atom]]
   [micro-rn.couchbase-lite :as cbl :refer [Server as-docs as-one-doc as-data-source aggregate <?
                                            get post delete put init view database document design-document attachment]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [liguria.server.main :as server :refer [db]])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(comment

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
                                                                                (rand-nth ["Спартак" "Зенит"
                                                                                           "Барселона" "Мадрид"])
                                                                                (rand-nth ["Торпедо" "Металург"
                                                                                           "Орландо" "Арсенал"]))}})}))))
        (>! port "[users messages complete]"))
      port))

  (defn fill-test-data []
    (let [port (chan)]
      (go
        ;; (println (<! (init-events)))
        ;; (println (<! (init-messages)))
        ;; (println (<! (init-bets)))
        ;; (println (<! (init-gifts)))
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
      port)))

