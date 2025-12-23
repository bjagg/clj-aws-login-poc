(ns aws-login.webapp.subs
  (:require [re-frame.core :as rf]
            [aws-login.webapp.jwt :as jwt]))

(rf/reg-sub :cfg (fn [db _] (:cfg db)))

;; --- routing ---------------------------------------------------------------

(rf/reg-sub :route/page (fn [db _] (get-in db [:route :page] :home)))
(rf/reg-sub :route/path (fn [db _] (get-in db [:route :path] "/")))

;; --- auth ------------------------------------------------------------------

(rf/reg-sub :auth/id-token (fn [db _] (get-in db [:auth :id-token])))
(rf/reg-sub :auth/access-token (fn [db _] (get-in db [:auth :access-token])))
(rf/reg-sub :auth/refresh-token (fn [db _] (get-in db [:auth :refresh-token])))
(rf/reg-sub :auth/status (fn [db _] (get-in db [:auth :status])))
(rf/reg-sub :auth/error (fn [db _] (get-in db [:auth :error])))

(rf/reg-sub
 :auth/logged-in?
 :<- [:auth/id-token]
 (fn [id _] (boolean (and id (not= id "")))))

(rf/reg-sub
 :auth/has-refresh?
 :<- [:auth/refresh-token]
 (fn [rt _] (boolean (and rt (not= rt "")))))

(defn- now-seconds [] (js/Math.floor (/ (.now js/Date) 1000)))

(defn- jwt-exp-seconds
  "Returns exp (seconds since epoch) from a JWT, or nil."
  [jwt-token]
  (when-let [p (jwt/decode-jwt-payload jwt-token)]
    (let [exp (aget p "exp")]
      (when (number? exp) exp))))

(rf/reg-sub
 :auth/access-exp
 :<- [:auth/access-token]
 (fn [at _] (jwt-exp-seconds at)))

(rf/reg-sub
 :auth/access-expires-in
 :<- [:auth/access-exp]
 (fn [exp _]
   (when (number? exp)
     (- exp (now-seconds)))))
