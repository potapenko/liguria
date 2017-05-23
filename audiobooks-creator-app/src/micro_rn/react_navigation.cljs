(ns micro-rn.react-navigation
  (:require
   [reagent.core :as r :refer [atom]]
   [reagent.impl.component :as ru]))

(defn import-module [name]
  (-> (js/require "react-navigation") (aget name) r/adapt-react-class))

(def create-navigation-container (import-module "createNavigationContainer"))
(def state-utils (import-module "StateUtils"))
(def add-navigation-helpers (import-module "addNavigationHelpers"))
(def navigation-actions (import-module "NavigationActions"))

;; Navigators
(def create-navigator (import-module "createNavigator"))
(def stack-navigator (import-module "StackNavigator"))
(def tab-navigator (import-module "TabNavigator"))
(def drawer-navigator (import-module "DrawerNavigator"))

;; Routers
(def stack-router (import-module "StackRouter"))
(def tab-router (import-module "TabRouter"))

;; Views
(def transitioner (import-module "Transitioner"))
(def card-stack (import-module "CardStack"))
(def card (import-module "Card"))

;; Header
(def header (import-module "Header"))
(def header-title (import-module "HeaderTitle"))
(def header-back-button (import-module "HeaderBackButton"))

;; DrawerView
(def drawer-view (import-module "DrawerView"))
(def drawer-items (import-module "DrawerItems"))

;; TabView
(def tab-view (import-module "TabView"))
(def tab-bar-top (import-module "TabBarTop"))
(def tab-bar-bottom (import-module "TabBarBottom"))

;; HOCs
(def with-navigation (import-module "withNavigation"))
