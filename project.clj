(defproject blorg "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  
  :main ^:skip-aot blorg.core
  :min-lein-version "2.7.1"
; https://github.com/reagent-project/reagent/blob/master/doc/CreatingReagentComponents.md
  :dependencies [
            ; [org.clojure/clojure "1.9.0"]
                 ; [org.clojure/clojurescript "1.10.238"]
                 ; [org.clojure/core.async  "0.4.474"]
                 ; [com.novemberain/monger "3.5.0" :exclusions [com.google.guava/guava]]
                 ; [cljsjs/react-dom-server "15.3.1-0"]  ;; for sablono
                 ; [cljsjs/react-dom "15.3.1-0"] ;; for sablono
                 ; [cljsjs/react "15.3.1-0"] ;; for sablono
                 ; [sablono "0.7.5"
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.3"]
                                        ; [rum "0.11.3"] ;-> https://github.com/tonsky/rum -> might be better
                ; [org.clojure/clojure "1.10.0"]
                 [clj-time "0.15.0"]
                 [digest "1.4.9"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [hiccup "1.0.5"]
                 [com.novemberain/monger "3.1.0" :exclusions [com.google.guava/guava]]
                 [clj-http "3.10.0"]
                 [cljs-http "0.1.46"]
                 [ring/ring-json "0.4.0"]
                 [cheshire "5.8.1"]
                 ;[cljs-ajax "0.8.0"]
                 [danlentz/clj-uuid "0.1.9"]
                 ]

  :plugins [[lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [yogthos/lein-sass "0.1.8"]
            [lein-ring "0.12.5"]
            ]
  ;:ring {:handler blorg.handler/http-handler}
  :source-paths ["src"]
  :sass {:source "resources/public/css/sass_resources" 
         :target "resources/public/css/sass_outputs"
         :hawk-options {:watcher :polling}}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                ;; The presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "blorg.core/on-js-reload"
                           ;; :open-urls will pop open your application
                           ;; in the default browser once Figwheel has
                           ;; started and compiled your application.
                           ;; Comment this out once it no longer serves you.
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main blorg.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/blorg.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :optimizations :none
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}
               ;; This next build is a compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/blorg.js"
                           :main blorg.core
                           :optimizations :none
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"
             ;; :ring-handler blorg.handler/routes
             :css-dirs ["resources/public/css"] ;; watch and update CSS
             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             :hawk-options {:watcher :polling}

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this

             ;; doesn't work for you just run your own server :) (see lein-ring)

             :ring-handler blorg.handler/http-handler
             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you are using emacsclient you can just use
             ;; :open-file-command "emacsclient"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"

             ;; to pipe all the output to the repl
             :server-logfile false
             }


  ;; Setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.16"]
                                  [cider/piggieback "0.3.1"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.2"]  
                                  ]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
