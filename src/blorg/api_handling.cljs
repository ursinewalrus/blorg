(ns blorg.api_handling
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [blorg.utils :as butils]))


#_(defn do-post
  [endpoint params callback]
  (go (let [response (<! (http/post endpoint {:with-credentials? false :query-params params}) )]
        (callback response)
)))

(defn get-posts
  [post-state]
  (go (let [response (<! (http/post "/posts" {:with-credentials? false :query-params {}}))]
        (swap!  post-state assoc :posts  (:body response))
        )))

(defn post-retrieve
  [post-state endpoint params]
  (go (let [response (<! (http/post endpoint {:query-params params}))]
        (prn (str  "making post call to " endpoint " with " params))
        (prn (str "response was "  (:body response)))
        (swap! post-state assoc :posts (:body response))
        )))

(defn send-auth-request
  [username password]
  (go (let [response 

            (->> 
             (<! (http/post "/auth-user" {:with-credentials? false :query-params {:username username :password password} }))
             :body
             (.parse js.JSON)
             (js->clj)
             )

            ]
        ;(js/console.log   (.parse js.JSON (:body response)))
        (if (contains? response "error") 
          (js/alert (get response "error"))
          (butils/set-auth-cookie response :refresh))   ;auth token
        )))

(defn log-in-oauth-user
  [oauth-map]
  (go (let [response (<! (http/post "/log-in-oauth-user" oauth-map))]
        (->>
         (:body response)
         (.parse js.JSON)
         (js->clj)
         (butils/set-auth-cookie)
         )
)))

;maaaybe
(defn handle-return-validation
  "db_interface passes back code to be executed if response contains specified params?"
  [post-response]

)

(defmacro handle-post-error
  [post-error]
)
