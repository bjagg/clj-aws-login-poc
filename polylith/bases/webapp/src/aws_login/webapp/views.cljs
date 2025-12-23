(ns aws-login.webapp.views
  (:require [re-frame.core :as rf]
            [aws-login.webapp.jwt :as jwt]))

(defn- pretty-json [x]
  (try
    (.stringify js/JSON x nil 2)
    (catch :default _
      (str x))))

(defn session-panel []
  (let [logged-in? @(rf/subscribe [:auth/logged-in?])
        has-refresh? @(rf/subscribe [:auth/has-refresh?])
        status @(rf/subscribe [:auth/status])
        err @(rf/subscribe [:auth/error])
        exp @(rf/subscribe [:auth/access-exp])
        expires-in @(rf/subscribe [:auth/access-expires-in])]
    [:div
     [:h3 "Session"]
     [:ul
      [:li [:strong "Logged in: "] (if logged-in? "yes" "no")]
      [:li [:strong "Status: "] (name (or status :unknown))]
      [:li [:strong "Has refresh token: "] (if has-refresh? "yes" "no")]
      [:li [:strong "Access token exp: "] (if exp (str exp) "n/a")]
      [:li [:strong "Access expires in (sec): "] (if (number? expires-in) (str expires-in) "n/a")]]
     (when err
       [:p [:strong "Error: "] err])
     [:div
      (when logged-in?
        [:button {:on-click #(rf/dispatch [:auth/refresh-now])}
         "Force refresh"])
      (when (and logged-in? (not has-refresh?))
        [:p "Note: no refresh token is available; when access expires you’ll need to log in again."])]]))

(defn claims-panel []
  (let [id-token @(rf/subscribe [:auth/id-token])
        js-claims (jwt/decode-jwt-payload id-token)]
    [:div
     [:h3 "ID token claims"]
     [:pre (if js-claims (pretty-json js-claims) "Not logged in.")]]))

(defn nav []
  (let [page @(rf/subscribe [:route/page])]
    [:div
     [:a {:href "#/" :style {:margin-right "12px"}}
      (if (= page :home) "Home" "Home")]
     [:a {:href "#/claims"} "Claims (guarded)"]]))

(defn home-page []
  (let [logged-in? @(rf/subscribe [:auth/logged-in?])
        status @(rf/subscribe [:auth/status])]
    [:div
     [:h2 "AWS Cognito + Google (PKCE) — re-frame PoC"]
     (when (= status :exchanging)
       [:p "Completing sign-in..."])
     [:div
      (when-not logged-in?
        [:button {:on-click #(rf/dispatch [:auth/login-clicked])} "Login"])
      (when logged-in?
        [:button {:on-click #(rf/dispatch [:auth/logout-clicked])} "Logout"])]
     [session-panel]]))

(defn claims-page []
  [:div
   [:h2 "Claims"]
   [claims-panel]])

(defn main []
  (let [page @(rf/subscribe [:route/page])]
    [:div
     [nav]
     (case page
       :claims [claims-page]
       ;; default
       [home-page])]))
