(ns aws-login.webapp.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [aws-login.webapp.events]
            [aws-login.webapp.subs]
            [aws-login.webapp.routes :as routes]
            [aws-login.webapp.views :as views]))

(defn ^:export init []
  (rf/dispatch-sync [:app/init])
  (when (= (routes/init!) :callback)
    (rf/dispatch [:oauth/handle-callback]))
  (rdom/render [views/main] (.getElementById js/document "app")))
