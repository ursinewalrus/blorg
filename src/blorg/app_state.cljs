(ns blorg.app_state
	(:require
         [reagent.core :as r]
         [goog.net.cookies :as cks]))


(defonce app-state 
  (r/atom {
           :posts nil
           :posts-ready true 
           :comment (str "") 
           :login-state nil
           :form-components {
                             ;don't quite match stuff checked in db
                             :title (str "")
                             :content (str "")
                             :comments (str "") ;for comments
                             :tags (str "")
                             :auth-user (.get goog.net.cookies "user" #_(:auth-user butils/cookie-keys))
                             :auth-token  (.get goog.net.cookies "token" #_(:auth-token butils/cookie-keys))
                             :auth-google  (.get goog.net.cookies "g-auth" #_(:auth-google butils/cookie-keys butils/cookie-keys))
                             }
           :auth-inputs {
                         :username (str "")
                         :password (str "")
                         }
}))
;to be used 
(defonce app-state2
  (r/atom {
           :mainpage {
                      :posts nil ;list of all posts
                      :posts-ready true ;refresh status? obselete
                      }
           :login-page {
                        :username (str "") 
                        :password (str "")
                        }
           :single-post {
                         :comment (str "")
                         :comments nil ; refresh on you adding one
                         }
           :auth-status {
                         :auth-user (.get goog.net.cookies "user")
                         :auth-token  (.get goog.net.cookies "token")
                         :auth-google  (.get goog.net.cookies "g-auth")
                         }
           :add-post {
                      :title (str "")
                      :content (str "")
                      :tags (str "")
                      }
           
}))
