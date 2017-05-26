(ns audiobooks-creator-app.app
  (:require audiobooks-creator-app.events
            [audiobooks-creator-app.screens.about-screen :as about]
            [audiobooks-creator-app.screens.books-screen :as books]
            [audiobooks-creator-app.screens.export-screen :as export]
            [audiobooks-creator-app.screens.friends-screen :as friends]
            [audiobooks-creator-app.screens.recording-screen :as recording]
            [audiobooks-creator-app.screens.settings-screen :as settings]
            [audiobooks-creator-app.screens.bookshelf-screen :as bookshelf]
            [audiobooks-creator-app.subs]
            [micro-rn.react-native :as rn]
            [micro-rn.react-navigation :as nav]
            [reagent.core :as r :refer [atom]]))

(def books-stack
  (nav/create-stack-navigator
   {:books {:screen books/main}}))

(def friends-stack
  (nav/create-stack-navigator
   {:friends {:screen friends/main}}))

(def main-tabs
  (nav/create-tab-navigator
   {:books-tab {:screen books-stack}
    :recording-tab {:screen recording/main}
    :bookshelf-tab {:screen bookshelf/main}
    :friends-tab  {:screen friends/main}
    :about-tab    {:screen about/main}}
   {:tab-bar-options {}}))

(defn app-root-component []
  [(r/adapt-react-class main-tabs)])
