(ns blorg.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET POST defroutes]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [hiccup.page :as h]
            [blorg.db_interface :as bdb]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [cheshire.core :refer :all]
            [blorg.utils :as butils]
            [digest :as digest]
))

(defn include-stylesheet
  [style-sheet]
  [:link {:rel "stylesheet" :href style-sheet}]
)

(defn render
  [route route-params]
  {
   :status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body   
   (h/html5 
    [:html
     [:head
      (h/include-css "css/sass_outputs/theme.css")
      (h/include-css "css/brutalist/core/core.css")]
     [:body {:style "background-image:url(\"images/invert-burger.png\")"}
       [:div {:id "react-zone" :class
              (cond
                (= route "home") "posts-page"
                (= route "single-post") "single-post"
                (= route "add_post") (do 
                                       (if (bdb/logged-in-user-is-admin (:token route-params)) 
                                         "add-posts "
                                         "auth-page"
                                         ))
                (= route "grid") "grid"
                :else "404"
                )}] 
      ;[:script {:src "js/compiled/google-api.js" :async "async" :defer "defer"}]
      [:script {:src "js/compiled/google-api.js"}]
      (h/include-js "js/compiled/jquery.3.4.1.js" #_"js/compiled/google-api.js" "js/compiled/blorg.js")
]])})

(defroutes routes
  ;every rought goes to posts, EVERY ROUTE
  (GET "/index.html" _ (render "home" _))
  (GET "/" _ (render "home" _)) 
  (GET "/add_post" _ #_(str  (butils/extract-cookies _ "token")) (render "add_post" 
                                                                         {:token (butils/extract-cookies _ "token")
                                                                          :user  (butils/extract-cookies _ "user")
                                                                          }))
  (GET "/post*" {params :query-params} (render "single-post" params))
  (GET "/grid" _ (render "grid" _))
  (POST "/submit-post" {:keys [params]} (generate-string (bdb/insert-post params)))
  (POST "/submit-comment"  {:keys [params]} (generate-string (bdb/insert-comment params)))
  (POST "/auth-user"  {:keys [params]} (bdb/authenticate-user (:username params) (digest/md5  (:password params))));admins hit this direct
  (POST "/log-in-oauth-user" {:keys [params]} (bdb/validate-oauth-user params))
  (POST "/posts" {:keys [params]}  {:status 200 
                               :headers {"Content-Type" "application/json"}
                               :body (generate-string (bdb/get-all-posts params))})
  (POST "/single-post" {:keys [params]} { :status 200
                                                     :headers {"Content-Type" "application/json"}
                                         :body  (generate-string (bdb/get-post-by-param params))})
  (POST "/param-test" {:keys [params]} (str "A" params))
  ;(POST "/test" "data" {:status 200 :headers {"Content-Type" "text/html; charset=utf-8"} :body "HERE"})
  ;(GET "about" _ (render "route" "params"))
  ;(GET "/" [] render)
  ;(GET "/about" [] render)
  ;(not-found "FOUR OH FOUR")
  )
;https://dhruvp.github.io/2015/02/23/mailchimip-clojure/ for aur widdle auth page

(def http-handler
  (-> routes
      #_(wrap-defaults site-defaults)
      (wrap-defaults api-defaults)
))

