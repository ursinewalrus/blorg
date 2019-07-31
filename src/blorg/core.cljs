(ns blorg.core
    (:require 
     [reagent.core :as r]
     [blorg.api_handling :as api-handler]
     [blorg.add_posts :as add_posts]
     [blorg.utils :as butils]
     [blorg.google_auth :as bga]
     [blorg.grid :as grid]
     [blorg.app_state :as as]
     [goog.net.cookies :as cks]
     )
    (:require-macros [cljs.core.async.macros :refer [go]])
)
;https://getmdl.io/started/index.html
(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload

 ;(defonce app-state (atom {:text "Hello world!"}))

; (defn on-js-reload []
;   ;; optionally touch your app-state to force rerendering depending on
;   ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
; )

(def shortcode-codes  {
                       :image "|i|"
                       }
)

(defn shortcode-check
  [token type]
  (and (clojure.string/starts-with? token type) (clojure.string/ends-with? token type))
)
;move to utils
(defn check-page
  [is-page]
  (< 0 (count (array-seq (js/document.getElementsByClassName is-page))))
)
(defn shortcode-replacer
  [token]
  (cond 
    ;check if image, do proper sub
    (shortcode-check token (get shortcode-codes :image))
    [:div [:img {:class "post-image" :src (str "images/" (clojure.string/replace token  (get shortcode-codes :image) ""))}]]
    ;nothing token
    :else
    (str " " token " ")
))

(defn sub-ins
  [content]
  (let [content-col (clojure.string/split content #"\s")]   
    (into [:div {:class "inner-post"}] (map shortcode-replacer content-col)) 
    )
)


(defn append-container
  [target]
  (.appendChild target (doto (.createElement js/document "div"))))

(defn comments-comp 
  [comments-vect]
  [:div {:class "post-comments " :id "comments-ancor"}
   (for [comment comments-vect]
     ^{:key (str (:user comment) "-" (:timestamp comment))}
     [:div {:class "comment"}
      [:div {:class "comment-text"}
       [:div {:class "comment-author"} (:user comment)] ;maybe a link?
       [:div {:class "comment-content"} (:comment comment)]
       [:div {:class "comment-time"} (:timestamp comment)]]
      [:img {:class "comment-author-img" :src (:user-image comment)}]
      ])])


(defn side-bar
  []
  #_[:div {:class "user-options"}
   [:span {:class "signup-link"} "Sign Up"]
   " or "
   [:span {:class "log-in"} "Log In"]
   ]
)

(defn render-posts
  []
  [:div {:class "posts-container"} 
    (for [post (:posts (deref as/app-state))]
        ^{:key (:_id post)}[:div {:class "peanut post"} 
                            [:div {:class "post-content"} 
                             [butils/post-link [:h4 {:class "post-title"} (:title post)] (:title post)]
                             (sub-ins (:content post))
                             (if (check-page "posts-page")
                               (do
                                 [butils/post-link [:div {:class "comments-link"} (str (count (:comments post)) " comments")] (:title post)]
                                 )
                               (do
                                 [:<> 
                                  (if (not (= nil  (.get goog.net.cookies (:auth-google butils/cookie-keys))))
                                    ^{:key (str "comment-" (:_id post))} [add_posts/comment-form (:title post)]
                                    [:div {:class "post-comment-login"} "Log in to comment"]
                                    )
                                  (comments-comp (:comments post))]
                                 )
                               )
                         
                             ]
                            [:hr {:class "post-seperator"}]])])

(defn site-sign-in-comp
  []
  [:button {:class "site-log-in-btn half-btn squared"} "Log In"]
)

(defn render-header
  []
[:<>
 [:div {:class "top"} [:<> [:div {:class "logins-div"}
                               [site-sign-in-comp] 
                               [bga/google-auth-comp]]
                          [:a {:class "header-link" :href "/grid"} "THE GRID"]
]] 
 [:div {:class "header bacon jagged2"}
  [:h1 {:class "title-area bacon"} 
   [:a  {:href "/"} [:span {:class "title-text  lime-text"} "Scrustinomicon"]]]]])

;test-posts from monger or something later
(defn create-page
  []
    [:div {:class "page-body"}
      [render-header]
     [:div {:id "center-area"}
      (cond
                                        ;this whole setup seems dubious
        (check-page "posts-page") (do (api-handler/get-posts) [render-posts])
        (check-page "single-post") (do  
                                     (api-handler/post-retrieve "/single-post" {:title  (butils/get-qs-param-value "title")}) 
                                     (swap! as/app-state assoc-in [:form-components :title] (butils/get-qs-param-value "title"))
                                     [render-posts]
                                     )
        (check-page "add-posts") [add_posts/add-post]
        (check-page "auth-page") [add_posts/auth-page]
        (check-page "grid") [grid/grid-comp]
        :else "FOUR OH FOUR")]
     (side-bar)
     ]
     
   )

(defn start-page
  []
    (r/render-component [create-page] (js/document.getElementById "react-zone")) ;create-page should be a variable from the route  
)

(start-page)

;coudl do a kinda getter/setter thing
;https://yogthos.net/posts/2014-07-15-Building-Single-Page-Apps-with-Reagent.html
