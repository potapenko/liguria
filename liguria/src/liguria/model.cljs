(ns liguria.model
  (:require
   [re-frame.core :refer [reg-event-db after dispatch-sync dispatch]]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [liguria.screens.recording.model :as recording-model]
   [liguria.screens.top.model :as top-model]
   [liguria.screens.wiki.model :as wiki-model]
   [liguria.screens.results.model :as results-model]
   [liguria.screens.recording.liguria-text :refer [liguria-text]]
   [liguria.screens.recording.nlp :as nlp])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   (go
     (loop [[v & t] (nlp/create-text-parts liguria-text)
            current []]
       (when v
        (<! (timeout 100))
        (dispatch-sync [::recording-model/transcript (conj current v)])
        (recur t (vec (conj current v)))))

     (dispatch-sync [::results-model/results-list (results-model/build-test-data)])
     (dispatch-sync [::top-model/top-list (top-model/build-test-data)])
     (dispatch-sync [::wiki-model/wiki-list (wiki-model/build-test-data)]))
   {}))

