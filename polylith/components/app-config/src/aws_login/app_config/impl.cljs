(ns aws-login.app-config.impl)

(def default-config
  {:cognito-domain ""
   :client-id ""
   :redirect-uri ""
   :logout-uri ""
   :scopes "openid email profile"})

(defn- js-config->clj [m]
  ;; Convert window.__APP_CONFIG__ into a CLJ map and normalize keys.
  (let [cfg (js->clj m :keywordize-keys true)]
    {:cognito-domain (or (get cfg :cognitoDomain) "")
     :client-id      (or (get cfg :clientId) "")
     :redirect-uri   (or (get cfg :redirectUri) "")
     :logout-uri     (or (get cfg :logoutUri) "")
     :scopes         (or (get cfg :scopes) "openid email profile")}))

(defn get-config []
  (let [m (.-__APP_CONFIG__ js/window)]
    (merge default-config (when m (js-config->clj m)))))
