(ns aws-login.webapp.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :cfg (fn [db _] (:cfg db)))

(rf/reg-sub :auth/id-token (fn [db _] (get-in db [:auth :id-token])))
(rf/reg-sub :auth/access-token (fn [db _] (get-in db [:auth :access-token])))
(rf/reg-sub :auth/status (fn [db _] (get-in db [:auth :status])))
(rf/reg-sub :auth/error (fn [db _] (get-in db [:auth :error])))

(rf/reg-sub
 :auth/logged-in?
 :<- [:auth/id-token]
 (fn [id _] (boolean (and id (not= id "")))))
