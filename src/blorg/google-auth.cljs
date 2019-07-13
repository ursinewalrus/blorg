(ns blorg.google_auth
  (:require [blorg.utils :as butils]
            [reagent.core :as r]
            [blorg.utils :as butils]
            [blorg.api_handling :as api-handler]
))

;fun with the interop


(defn logged-in-with-g
  [google-user]
;gapi.auth2.getAuthInstance().disconnect();
  (let [g-token (.. google-user (getAuthResponse) -id_token)]
    (butils/set-gauth-cookie g-token)
    (api-handler/log-in-oauth-user {:query-params {:type "google" :token  g-token} :with-credentials? false})
))

(defn create-oauth-button-g
  [] 
  (js/gapi.signin2.render "g-signin2" #js {"scope"     "profile email"
                                           "width"     240
                                           "height"    50
                                           "longtitle" true
                                           "theme"     "dark"
                                           "onsuccess" logged-in-with-g
                                           "onfailure" prn
}))


(defn google-auth-comp
  []
   ;[:div {:class "g-signin2" :data-theme "dark" :data-onsuccess "onSignIn"}]
   ;[:div {:id "g-signin2"}]
   (r/create-class {
                    ;http://reagent-project.github.io/docs/master/reagent.core.html
                    :display-name "google-auth-comp"
                    :component-did-mount (fn [this] 
                                           (create-oauth-button-g) 
                                           )
                    :reagent-render (fn [this]  
                                      [:<> 
                                       [:meta {:name "google-signin-scope" :content "profile email"}]
                                       [:meta {:name "google-signin-client_id" :content "760215270982-vlf02qrrlgt39kfli5ogt5379d1dubll.apps.googleusercontent.com"}]
                                       [:div {:id "g-signin2"}]
                                       (create-oauth-button-g)
                                       ]
                                      )
                          }))






