(ns liguria.screens.results.model
  (:require [re-frame.core :refer [reg-sub reg-event-db dispatch dispatch-sync subscribe]]
            [clojure.core.reducers :as red]
            [micro-rn.rn-utils :as rn-utils]
            [micro-rn.utils :as utils]
            [clojure.string :as string]
            [micro-rn.react-native :as rn]
            [cljs.core.async :as async :refer [<! >! put! chan timeout]])
  (:require-macros [micro-rn.macros :refer [...]]
                   [cljs.core.async.macros :refer [go go-loop]]))
