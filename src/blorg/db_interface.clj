(ns blorg.db_interface
  (:require [monger.core :as monger]
            [monger.credentials :as mcred]
            [monger.collection :as mc]
            [monger.json :as mj]
            [monger.operators :refer :all]
            [blorg.utils :as butils]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-http.client :as http]
            [blorg.config :as config]
            )
  (:import [com.mongodb MongoOptions ServerAddress])
)


#_(defonce db-context (atom ( 
                           monger/connect-with-credentials
                           "127.0.0.1"
                           (mcred/create
                            "username"
                            "admin"
                            (.toCharArray "password"));if youre gonna put this on git...
                           )))

(defonce blorg-context-atom (atom (monger/get-db (monger/connect) "blorg")))
(defn blorg-context
  []
  @blorg-context-atom
  #_(monger/get-db (monger/connect) "blorg")
)



(defmacro users-col
  "query params"
  [query params]
  `(~query (blorg-context) "blorg_users" ~params)
)

#_(defmacro validate-admin-token
  [to-query-hash]
  (str "WIP")
)



(defn get-all-posts
  [params]
  ;only working with x-www-form-urlencoded data, look into raw
  (reverse (sort-by :timestamp (mc/find-maps (blorg-context) "posts" params #_(into params {:tags ["test" "secret"]})))))

(defn get-post-by-param
  [param]
  (mc/find-maps (blorg-context) "posts" param)
)

(defn generate-session-token-for-user
  [user]
  (let [token (butils/generate-token-uuid)]
    (mc/update-by-id (blorg-context) "blorg_users" (:_id user) {$set {:token (str token) :token-expire (str (t/plus (t/now) (t/hours 12) )) }}) 
    (generate-string {:token (str token "") :user (:username user)})))

(defn authenticate-user
  [username hashed-pw]
  (let [user  (mc/find-one-as-map (blorg-context) "blorg_users" {$or [{:username username} {:email username}] :password hashed-pw})]
    (if (=  (:username user) username)
      (generate-session-token-for-user user)
      (generate-string {:error "Invalid Username/Email or Password"})
      )))

(defn user-exists
  [param]
  (let [user (users-col mc/find-one-as-map param)]
    (if (= nil user)
      false
      user
      )))

(defn logged-in-user-is-admin
  [token]
  ;privs contains type 
  (let [user (mc/find-one-as-map (blorg-context) "blorg_users" {:token token})]     
    (if (and user (some #(= "admin" %) (:privs user)))
         true
         false
    )
))



(defn parse-gtoken-info
  [info]
  (let [{status :status data :body} info]
    (if (not= 200 status)
      (generate-string {:error "Invalid Token"})
      (let [user (user-exists {:email  (get data "email") :sub (get data "sub")}) valid-token (butils/validate-g-token data)]
        (if valid-token;expire??
          (if user
            (generate-session-token-for-user user)
            (do
              (-> 
               ;creates google user
               (users-col mc/save-and-return {:email (get data "email") :username (get data "name") :image (get data "picture") :sub (get data "sub")})
               (generate-session-token-for-user)
               )
              ))
          {:error "Invalid or expired token"}
)))))

;gets the :type and  & rest
(defn validate-oauth-user
  [params] ;lets get a main store for these keys 
  (let [{type :type token :token} params]
    (cond 
      (= type "google") (->>  (butils/check-google-token token) (parse-gtoken-info)))
))

(defn validate-post-req
"if a valid post returns the post, if not returns a {:error 'message'}"
  [post-params]
  (let [valid-req (mc/find-one-as-map (blorg-context) "blorg_users" {$and [{:username (:auth-user post-params)} {:token (:auth-token post-params)}]} )]
    (if (contains? valid-req :username)
      (if (< (compare (str (t/now)) (:token-expire valid-req)) 0)
        post-params
        {:error "Session expired, re-Login"}
        )
      {:error (str "Invalid request: " post-params)}
      )
))

(defmacro insert-content
  [content-params insert-fn]
  `(let [valid-post# (validate-post-req ~content-params)]
     ;valid-post#
     (if (contains? valid-post# :error)
       valid-post#
       ~insert-fn)))

(defn insert-comment ; need to get the poster's image
  [comment-params]
  (let [post (mc/find-one-as-map (blorg-context) "posts" {:title (:title comment-params)})]
    (insert-content comment-params
                    (do (mc/update-by-id (blorg-context) "posts" (:_id post) 
                                         {$addToSet {:comments {:user (:auth-user comment-params)  
                                                                :user-image (:image (users-col mc/find-one-as-map {:username (:auth-user comment-params)}))
                                                                :comment (:comment comment-params) 
                                                                :timestamp (str (t/now))}} })  
                        {:success "Comment added"}))))

(defn insert-post
   [post-params]
  (if (logged-in-user-is-admin (:post-auth-token post-params)); and validate-post-req
    (insert-content post-params (do
                   (mc/save (blorg-context) "posts"
                            {:title (:title post-params) 
                             :content (:content post-params) 
                             :tags (:tags post-params) 
                             :timestamp (str (t/now))})
         {:success "Post Added"}))))

