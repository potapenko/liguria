(ns liguria.app
  (:require [liguria.screens.books.views :as books]
            [liguria.screens.recording.views :as recording]
            [liguria.screens.friends.views :as friends]
            [liguria.screens.bookshelf.views :as bookshelf]
            [liguria.screens.settings.views :as settings]
            [liguria.screens.more.views :as more]
            [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]
            [micro-rn.couchbase-lite :as couchbase]))

(def books-stack
  (nav/create-stack-navigator
   {:books {:screen books/main}}))

(def recording-stack
  (nav/create-stack-navigator
   {:books {:screen recording/main}}))

(def friends-stack
  (nav/create-stack-navigator
   {:friends {:screen friends/main}}))

(def main-tabs
  (nav/create-tab-navigator
   {:recording-tab {:screen recording-stack}
    :books-tab     {:screen books-stack}
    :bookshelf-tab {:screen bookshelf/main}
    :friends-tab   {:screen friends/main}
    :more-tab      {:screen more/main}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])

