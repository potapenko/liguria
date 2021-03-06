(defproject liguria "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [org.clojure/clojurescript "1.9.542"]
                 [reagent "0.7.0" :exclusions [cljsjs/react cljsjs/react-dom
                                               cljsjs/react-dom-server cljsjs/create-react-class]]
                 [re-frame "0.9.2"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [camel-snake-kebab "0.4.0"]
                 [mount "0.1.11"]
                 [org.clojure/core.async "0.3.443"]
                 [thi.ng/color "1.2.0"]
                 [thi.ng/math "0.2.1"]
                 [natal-shell "0.5.1"]]
  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.10"]]
  :clean-targets ["target/" "index.ios.js" "index.android.js" #_($PLATFORM_CLEAN$)]
  :aliases {"prod-build" ^{:doc "Recompile code with prod profile."}
            ["do" "clean"
             ["with-profile" "prod" "cljsbuild" "once"]]}
  :profiles {:dev  {:dependencies [[figwheel-sidecar "0.5.10"]
                                   [com.cemerick/piggieback "0.2.1"]]
                    :source-paths ["src" "env/dev"]
                    :cljsbuild    {:builds [{:id           "ios"
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to       "target/ios/not-used.js"
                                                            :main            "env.ios.main"
                                                            :output-dir      "target/ios"
                                                            :closure-defines {"clairvoyant.core.devmode" true}
                                                            :optimizations   :none}}
                                            {:id           "android"
                                             :source-paths ["src" "env/dev"]
                                             :figwheel     true
                                             :compiler     {:output-to     "target/android/not-used.js"
                                                            :main          "env.android.main"
                                                            :output-dir    "target/android"
                                                            :optimizations :none}}
                                            #_($DEV_PROFILES$)]}
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
             :prod {:cljsbuild {:builds [
                                         {:id           "ios"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to          "index.ios.js"
                                                         :main               "env.ios.main"
                                                         :output-dir         "target/ios"
                                                         :static-fns         true
                                                         :optimize-constants true
                                                         :optimizations      :simple}}
                                         {:id           "android"
                                          :source-paths ["src" "env/prod"]
                                          :compiler     {:output-to          "index.android.js"
                                                         :main               "env.android.main"
                                                         :output-dir         "target/android"
                                                         :static-fns         true
                                                         :optimize-constants true
                                                         :optimizations      :simple
                                                         :closure-defines    {"goog.DEBUG" false}}}
                                         #_($PROD_PROFILES$)]}}})


