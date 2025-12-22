(ns aws-login.webapp.events
  (:require [re-frame.core :as rf]
            [aws-login.app-config.api :as cfg]
            [aws-login.oauth-pkce.api :as pkce]
            [aws-login.oauth-pkce.oauth-storage :as store]
            [aws-login.webapp.routes :as routes]))

(rf/reg-event-db
 :app/init
 (fn [_ _]
   {:cfg (cfg/get-config)
    :auth {:id-token (store/read-id-token)
           :access-token (store/read-access-token)
           :status :idle
           :error nil}}))

(rf/reg-event-fx
 :auth/login-clicked
 (fn [{:keys [db]} _]
   (pkce/start-login! (:cfg db))
   {}))

(rf/reg-event-fx
 :auth/logout-clicked
 (fn [{:keys [db]} _]
   (store/clear-tokens!)
   (set! (.-location js/window) (pkce/logout-url (:cfg db)))
   {}))

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
                      at (aget tokens "access_token")]
                  (store/store-tokens! {:id-token id :access-token at})
                  (store/clear-verifier!)
                  (routes/replace-state! "/")
                  (rf/dispatch [:auth/tokens-updated id at]))))
       (.catch (fn [e]
                 (rf/dispatch [:auth/error (or (.-message e) (str e))]))))))

(rf/reg-event-db
 :auth/tokens-updated
 (fn [db [_ id access]]
   (-> db
       (assoc-in [:auth :id-token] id)
       (assoc-in [:auth :access-token] access)
       (assoc-in [:auth :status] :idle))))

(rf/reg-event-db
 :auth/error
 (fn [db [_ msg]]
   (-> db
       (assoc-in [:auth :status] :error)
       (assoc-in [:auth :error] msg))))
