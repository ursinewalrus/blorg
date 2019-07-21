(ns blorg.add_posts
    (:require 
     [reagent.core :as r]
     [blorg.utils :as butils]
     [blorg.api_handling :as api-handler]
     [blorg.app_state :as as]
     ;[cljs-http.client :as http]
     ;[cljs.core.async :refer [<!]]
     ;[goog.net.cookies :as cks]
     ;[reagent.cookies :as rutils]
))


(defn auth-elements
  ([extras]
   [:div {:class "hidden-element"}
    (butils/general-input :input "auth-user" "post-auth-user" ""  :require "text" as/app-state [:form-components :auth-user] "form-group" "post-auth-user")
    (butils/general-input :input "auth-token" "post-auth-token" ""  :require "text" as/app-state [:form-components :auth-token] "form-group" "post-auth-token")
    (for [[extra-k extra-v] extras] (butils/general-input :input (str "post-auth-extra-" extra-k)  (str "post-auth-extra-" extra-k) "" :require "text" extra-v [] "form-group" (str "post-auth-" extra-k) ))
    ])
  ([]
   (auth-elements {}))
)

(defn add-post
  []
  [:div {:class "center-area form-container peanut "}   
   [:form {:class "post-form padding" :method :post :action "/submit-post"}
    (butils/general-input :input "post-title" "title" "Post Title" :required "text" as/app-state [:form-components :title] "form-group" "post-title")
    (butils/general-input :textarea "post-content" "content" "Post Content" :required "text" as/app-state [:form-components :content] "form-group" "blorg-form")
    (butils/general-input :input "post-tags" "tags" "Post Tags" :notrequire "text" as/app-state [:form-components :tags] "form-group" "post-tags")
    (auth-elements)
    [:button {:type :submit :class "squared full-btn"} "Add Post"]
    ]])


(defn comment-form-submit-callback
  [e]
 (.preventDefault e)
  (api-handler/send-post-request "/submit-comment" 
                                 (into (:form-components @as/app-state) {:comment (:comment @as/app-state)})
                                 api-handler/post-retrieve 
                                 ["/single-post" {:title  (butils/get-qs-param-value "title")}])
  ;clear the form and something idk 
)

(defn comment-form
  [id]
  ^{:key (str "comment-form-" id)} [:form {:class "comment-form padding" :method :post :action "/submit-comment" :on-submit comment-form-submit-callback}
                                    ^{:key (str "comment-input-id-" id)} 
                                    (butils/general-input :textarea "comment" "comment"  "Comment" :require "text" as/app-state [:comment] "comment-form" "form-group")
       (auth-elements {"post-title" id})
       [:button {:type :submit :class "squared half-btn"} "Submit Comment"]
       ])

;for admin sign in
(defn auth-form
  []
   [:form {:id "auth-form" :class "post-form padding" :method :post :action "/auth-user" :on-submit (fn [e]
                                                                                                      (.preventDefault e)
                                                                                                            (api-handler/send-auth-request 
                                                                                                             (butils/get-element-value "username")
                                                                                                             (butils/get-element-value "password")
                                                                                                             ))}
    (butils/general-input :input "username" "username" "Username" :required "text" as/app-state [:auth-inputs :username] "form-group" "username-input")
    (butils/general-input :input "password" "password" "Password" :required "password" as/app-state [:auth-inputs :password] "form-group" "password-input")
    [:button {:type :submit :class "squared half-btn"} "Log In"]
])

;for admin sign in
(defn auth-page
  []
  [:div {:class "center-area form-container peanut"}
   [auth-form]
])
