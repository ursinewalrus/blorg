(ns blorg.utils
  (:require 
   [clojure.string :as cs]
   #?( :clj [digest :as digest])
   #?( :clj [clj-uuid :as uuid])
   #?( :clj [clj-http.client :as http])
   #?( :clj [blorg.config :as config])
   #? (:clj [clj-time.core :as t])
   #? (:clj [clj-time.coerce :as tc])
   #?( :cljs [goog.net.cookies :as cks])
   #? (:clj [cheshire.core :refer :all])
))
;overkill for this kind of thing but why not
(def cookie-keys {
                  :auth-user "user" ;for admins only atm
                  :auth-token "token" ;for admins only atm
                  :auth-google "g-auth"
})

#?(
   :clj
   (defn extract-cookies 
     ([headers]
      (-> headers 
          :headers 
          (get "cookie")
          (str "")
          ))
     ([headers cookie]
      
      (-> 
       (filterv #(cs/includes? % cookie) 
                (->  (extract-cookies headers)
                     (cs/split #";" )
                     ))
       (first)
       (str)
       (cs/split #"=")
       (second)
       ))))

#?(
   :clj
   (defn generate-token-uuid
     []
     (uuid/v1)

))


#?(
   :clj
   (defn check-google-token
     [token]
     (let [response  (http/get (str "https://oauth2.googleapis.com/tokeninfo?id_token=" token))]
       {:status (:status response) :body (parse-string (:body response))}
       )))


#?(
   :clj
   (defn validate-g-token
     [token-info]
     (prn (str "what " token-info))
     (if (and  (not= (get token-info "aud") (-> config/oauth :google-settings :iss)) (= (get token-info "azp") (-> config/oauth :google-settings :azp)) (some #{(get token-info "iss")} (-> config/oauth :google-settings :iss) ) #_(< (compare (get token-info "exp") (str (tc/to-long (t/now))) 0)))
       true
       false
)))

#?(
   :default
   (defn link-to-post
     [post-title]
     (str "post?title=" post-title)
))

#?(
   :default
   (defn pp
     [var]
     (str "actual: " var " type: " (type var) " count: " (count var))))

#?(
 :cljs
 [(defn general-input
     "type id name label require input-type  default-values & classes"
     [type id name label require input-type  stateful-val & classes]
     [:div {:class "form-group"}
      [:div 
       [:label {:for (str name " input-label form-label") :class "form-label"} label
        ]]
      [type  {
              :id id
              :name name
              :class (clojure.string/join " " classes)
              require "true"
              :value stateful-val
              :type input-type
              }]])

  (defn set-auth-cookie
    [auth-hash & refresh]
    (prn (pp auth-hash))
    ;maybe change to use the reagent-utils cookies
    (.set goog.net.cookies (:auth-user cookie-keys) (auth-hash "user") 21600)
    (.set goog.net.cookies (:auth-token cookie-keys) (auth-hash "token") 21600) ;uh...12 hours?
    (if (= refresh :refresh)
        (set! (.-href window.location) window.location));...make it pass along where you actually wanted to go
    )

  (defn set-gauth-cookie
    [token]
    (.set goog.net.cookies (:auth-google cookie-keys) token 3600)
)

  (defn set-cookie
    [cookie-key cookie-val]
    (.set goog.net.cookies cookie-key cookie-val)
    )

  (defn get-qs-param-value
    [param]
    (->
     (.-href window.location)
     (cs/split #"\?")
     ((fn [argv] (filterv #(cs/includes? % (str param "=")) argv )))
     (first)
     (str)
     (cs/split #"=")
     (second)
     (cs/replace "%20" " ");maybe get like, an actual decoder func here
     ))


  (defn post-link
    [r-vect post-title]
    [:a {:class "post-title-link" :href (link-to-post post-title)} r-vect]
    )

   (defn get-element-value
     [id]
     (.-value (.getElementById js/document id)))])

