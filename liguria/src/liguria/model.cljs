(ns liguria.model
  (:require
   [re-frame.core :refer [reg-event-db after dispatch-sync dispatch]]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [liguria.screens.recording.model :as recording-model]
   [liguria.screens.top.model :as top-model]
   [liguria.screens.wiki.model :as wiki-model]
   [liguria.screens.results.model :as results-model]
   [liguria.shared.liguria-text :refer [liguria-text]]
   [liguria.shared.nlp :as nlp])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   (dispatch [::results-model/results-list (results-model/build-test-data)])
   (dispatch [::top-model/top-list (top-model/build-test-data)])
   (dispatch [::wiki-model/wiki-list (wiki-model/build-test-data)])
   {}))

