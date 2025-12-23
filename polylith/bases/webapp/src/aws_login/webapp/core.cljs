(ns aws-login.webapp.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [aws-login.webapp.events]
            [aws-login.webapp.subs]
            [aws-login.webapp.views :as views]
            [aws-login.webapp.routes :as routes]))

(defonce stop-router! (atom nil))

(defn ^:private mount-root []
  (rdom/render [views/main] (.getElementById js/document "app")))

(defn init []
  (rf/dispatch-sync [:app/init])

  ;; If we are on /callback, handle it immediately.
  (when (= (routes/page) :callback)
    (rf/dispatch [:oauth/handle-callback]))

  ;; Start minimal hash router and keep app-db in sync.
  (reset! stop-router!
          (routes/start! (fn [route]
                           (rf/dispatch [:route/changed route]))))

  ;; Best-effort refresh (if access token expires soon and refresh token exists)
  (rf/dispatch [:auth/ensure-fresh])

  (mount-root))
