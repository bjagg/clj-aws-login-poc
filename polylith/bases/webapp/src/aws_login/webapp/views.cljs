(ns aws-login.webapp.views
  (:require [re-frame.core :as rf]
            [aws-login.webapp.jwt :as jwt]))

(defn claims-panel []
  (let [id-token @(rf/subscribe [:auth/id-token])
        js-claims (jwt/decode-jwt-payload id-token)]
    [:pre {:style {:white-space "pre-wrap"}}
     (if js-claims
       (.stringify js/JSON js-claims nil 2)
       "Not logged in.")]))

(defn main []
  (let [logged-in? @(rf/subscribe [:auth/logged-in?])
        status @(rf/subscribe [:auth/status])
        err @(rf/subscribe [:auth/error])]
    [:div {:style {:font-family "system-ui, -apple-system, Segoe UI, Roboto, sans-serif"
                   :max-width "900px"
                   :margin "2rem auto"
                   :padding "0 1rem"}}
     [:h1 "AWS Cognito Login (re-frame + PKCE)"]

     (when (= status :exchanging)
       [:div {:style {:margin "1rem 0" :padding "0.5rem" :background "#fff3cd"}}
        "Completing sign-in..."])

     (when err
       [:div {:style {:margin "1rem 0" :padding "0.5rem" :background "#f8d7da"}}
        [:strong "Error: "] err])

     [:div {:style {:display "flex" :gap "0.75rem" :margin "1rem 0"}}
      (when-not logged-in?
        [:button {:on-click #(rf/dispatch [:auth/login-clicked])} "Login with Google"])
      (when logged-in?
        [:button {:on-click #(rf/dispatch [:auth/logout-clicked])} "Logout"])]

     [claims-panel]]))
