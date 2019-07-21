(ns blorg.api_handling
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r]
            [blorg.app_state :as as]
            [blorg.utils :as butils]))


#_(defn do-post
  [endpoint params callback]
  (go (let [response (<! (http/post endpoint {:with-credentials? false :query-params params}) )]
        (callback response)
)))

(defn get-posts
  []
  (go (let [response (<! (http/post "/posts" {:with-credentials? false :query-params {}}))]
        (swap! as/app-state assoc :posts  (:body response))
        )))

(defn post-retrieve
  [endpoint params]
  (prn endpoint)
  (prn params)
  (go (let [response (<! (http/post endpoint {:query-params params}))]
        (prn "doin the swap")
        (swap! as/app-state assoc :posts (:body response))
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


(defn handle-return-validation
  [post-response]
  (if (contains? post-response :error)
    false
    true
))

(defn send-post-request
  [endpoint data callback & cb-arg-vect]
  (go (let [response (<! (http/post endpoint {:with-credentials? false :query-params data}))]
        (if (handle-return-validation (:body response))
          (do
           ;(post-retrieve "/single-post" {:title  (butils/get-qs-param-value "title")}) ; really bad obvi, should be a callback or something
            (apply callback (flatten cb-arg-vect))
            )
))))


(defmacro handle-post-error
  [post-error]
)
