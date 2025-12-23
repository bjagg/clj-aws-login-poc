(ns aws-login.webapp.events
  (:require [re-frame.core :as rf]
            [aws-login.app-config.api :as cfg]
            [aws-login.oauth-pkce.api :as pkce]
            [aws-login.webapp.jwt :as jwt]
            [aws-login.webapp.routes :as routes]))

;; ---------------------------------------------------------------------------
;; Token lifetime helpers
;; ---------------------------------------------------------------------------

(defn- now-seconds [] (js/Math.floor (/ (.now js/Date) 1000)))

(defn- jwt-exp-seconds
  "Returns exp (seconds since epoch) from a JWT, or nil."
  [jwt-token]
  (when-let [p (jwt/decode-jwt-payload jwt-token)]
    (let [exp (aget p "exp")]
      (when (number? exp) exp))))

(defn- expiring-soon?
  "True if exp is within `skew` seconds from now."
  [jwt-token skew]
  (when-let [exp (jwt-exp-seconds jwt-token)]
    (<= (- exp (now-seconds)) skew)))

;; ---------------------------------------------------------------------------
;; App init
;; ---------------------------------------------------------------------------

(rf/reg-event-db
 :app/init
 (fn [_ _]
   (let [id (pkce/read-id-token)
         at (pkce/read-access-token)
         rt (pkce/read-refresh-token)]
     {:cfg (cfg/get-config)
      :auth {:id-token id
             :access-token at
             :refresh-token rt
             :status :idle
             :error nil}})))

;; Call this after app/init (and before protected API calls).
(rf/reg-event-fx
 :auth/ensure-fresh
 (fn [{:keys [db]} _]
   (let [at (get-in db [:auth :access-token])
         rt (get-in db [:auth :refresh-token])]
     (when (and at rt (expiring-soon? at 60))
       {:db (assoc-in db [:auth :status] :refreshing)
        :fx [[:oauth/refresh-tokens {:cfg (:cfg db) :refresh-token rt}]]}))))

;; Manual refresh button (for learning/debugging)
(rf/reg-event-fx
 :auth/refresh-now
 (fn [{:keys [db]} _]
   (let [rt (get-in db [:auth :refresh-token])]
     (when rt
       {:db (assoc-in db [:auth :status] :refreshing)
        :fx [[:oauth/refresh-tokens {:cfg (:cfg db) :refresh-token rt}]]}))))

;; ---------------------------------------------------------------------------
;; Login / Logout
;; ---------------------------------------------------------------------------

(rf/reg-event-fx
 :auth/login-clicked
 (fn [{:keys [db]} _]
   (pkce/start-login! (:cfg db))
   {}))

(rf/reg-event-fx
 :auth/logout-clicked
 (fn [{:keys [db]} _]
   (pkce/clear-tokens!)
   (set! (.-location js/window) (pkce/logout-url (:cfg db)))
   {}))

;; ---------------------------------------------------------------------------
;; Callback handling
;; ---------------------------------------------------------------------------

(rf/reg-event-fx
 :oauth/handle-callback
 (fn [{:keys [db]} _]
   (let [err (routes/oauth-error)
         code (routes/oauth-code)]
     (cond
       err
       {:db (assoc-in db [:auth :error] (str err))}

       (nil? code)
       {}

       :else
       {:db (-> db
                (assoc-in [:auth :status] :exchanging)
                (assoc-in [:auth :error] nil))
        :fx [[:dispatch [:oauth/exchange code]]]}))))

(rf/reg-event-fx
 :oauth/exchange
 (fn [{:keys [db]} [_ code]]
   {:fx [[:oauth/exchange-tokens {:cfg (:cfg db) :code code}]]}))

(rf/reg-fx
 :oauth/exchange-tokens
 (fn [{:keys [cfg code]}]
   (-> (pkce/exchange-code! cfg code)
       (.then (fn [tokens]
                (let [id (aget tokens "id_token")
                      at (aget tokens "access_token")
                      rt (aget tokens "refresh_token")] ; may be nil
                  (pkce/store-tokens! {:id-token id :access-token at :refresh-token rt})
                  (pkce/clear-verifier!)
                  (routes/replace-state! "/")
                  (rf/dispatch [:auth/tokens-updated id at (or rt (pkce/read-refresh-token))]))))
       (.catch (fn [e]
                 (rf/dispatch [:auth/error (or (.-message e) (str e))]))))))

;; ---------------------------------------------------------------------------
;; Refresh flow
;; ---------------------------------------------------------------------------

(rf/reg-fx
 :oauth/refresh-tokens
 (fn [{:keys [cfg refresh-token]}]
   (-> (pkce/refresh-tokens! cfg refresh-token)
       (.then (fn [tokens]
                ;; Cognito returns a new access_token (and sometimes id_token).
                ;; refresh_token is usually NOT returned on refresh grant unless rotation is enabled.
                (let [id (or (aget tokens "id_token") (pkce/read-id-token))
                      at (or (aget tokens "access_token") (pkce/read-access-token))
                      rt (or (aget tokens "refresh_token") refresh-token)]
                  (pkce/store-tokens! {:id-token id :access-token at :refresh-token rt})
                  (rf/dispatch [:auth/tokens-updated id at rt]))))
       (.catch (fn [e]
                 (pkce/clear-tokens!)
                 (rf/dispatch [:auth/error (str "Refresh failed: " (or (.-message e) (str e)))]))))))

;; ---------------------------------------------------------------------------
;; State updates
;; ---------------------------------------------------------------------------

(rf/reg-event-db
 :auth/tokens-updated
 (fn [db [_ id access refresh]]
   (-> db
       (assoc-in [:auth :id-token] id)
       (assoc-in [:auth :access-token] access)
       (assoc-in [:auth :refresh-token] refresh)
       (assoc-in [:auth :status] :idle))))

(rf/reg-event-db
 :auth/error
 (fn [db [_ msg]]
   (-> db
       (assoc-in [:auth :status] :error)
       (assoc-in [:auth :error] msg))))
