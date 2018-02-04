(ns micro-rn.google-api
  (:require
   [reagent.core :as r :refer [atom]]
   [clojure.string :as string]
   [micro-rn.utils :as utils]
   [cljs.core.async :as async :refer [<! >! put! chan timeout]]
   [micro-rn.react-native :as rn])

  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))

(def LoginManager (aget (js/require "react-native-google-signin") "GoogleSignin"))
(def GoogleSigninButton (aget (js/require "react-native-google-signin") "GoogleSigninButton"))

(def signin-button (rn/adapt-react-class GoogleSigninButton "google-signin-button"))
