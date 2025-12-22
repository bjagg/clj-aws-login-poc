(ns aws-login.oauth-pkce.api
  "Stable public API for the oauth-pkce component.
   Other bricks should depend on this namespace, not on `impl`."
  (:require [aws-login.oauth-pkce.impl :as impl]
            [aws-login.oauth-pkce.oauth-storage :as store]))

(defn start-login! [cfg] (impl/start-login! cfg))
(defn exchange-code! [cfg code] (impl/exchange-code! cfg code))
(defn logout-url [cfg] (impl/logout-url cfg))

;; storage helpers that callers may want
(defn read-id-token [] (store/read-id-token))
(defn read-access-token [] (store/read-access-token))
(defn store-tokens! [m] (store/store-tokens! m))
(defn clear-tokens! [] (store/clear-tokens!))
(defn clear-verifier! [] (store/clear-verifier!))
