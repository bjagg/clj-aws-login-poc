(ns aws-login.oauth-pkce.oauth-storage)

;; NOTE (PoC):
;; We store tokens in localStorage for simplicity.
;; For production, prefer a Backend-for-Frontend (BFF) so refresh tokens are never exposed to JS.

(def pkce-verifier-key "pkce_verifier")

(def id-token-key "id_token")
(def access-token-key "access_token")
(def refresh-token-key "refresh_token")

;; --- PKCE verifier (session-scoped) -----------------------------------------

(defn store-verifier! [v] (.setItem js/sessionStorage pkce-verifier-key v))
(defn read-verifier [] (.getItem js/sessionStorage pkce-verifier-key))
(defn clear-verifier! [] (.removeItem js/sessionStorage pkce-verifier-key))

;; --- localStorage helpers ---------------------------------------------------

(defn- ls-get [k] (.getItem js/localStorage k))
(defn- ls-set! [k v] (.setItem js/localStorage k v))
(defn- ls-del! [k] (.removeItem js/localStorage k))

(defn read-id-token [] (ls-get id-token-key))
(defn read-access-token [] (ls-get access-token-key))
(defn read-refresh-token [] (ls-get refresh-token-key))

(defn store-tokens!
  "Stores tokens. Refresh token is only written if present (don’t overwrite with nil)."
  [{:keys [id-token access-token refresh-token]}]
  (when id-token (ls-set! id-token-key id-token))
  (when access-token (ls-set! access-token-key access-token))
  (when refresh-token (ls-set! refresh-token-key refresh-token)))

(defn clear-tokens! []
  (ls-del! id-token-key)
  (ls-del! access-token-key)
  (ls-del! refresh-token-key))
