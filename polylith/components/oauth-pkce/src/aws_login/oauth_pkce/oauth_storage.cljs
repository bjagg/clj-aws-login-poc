(ns aws-login.oauth-pkce.oauth-storage)

(def pkce-verifier-key "pkce_verifier")
(def id-token-key "id_token")
(def access-token-key "access_token")

(defn store-verifier! [v] (.setItem js/sessionStorage pkce-verifier-key v))
(defn read-verifier [] (.getItem js/sessionStorage pkce-verifier-key))
(defn clear-verifier! [] (.removeItem js/sessionStorage pkce-verifier-key))

(defn ls-get [k] (.getItem js/localStorage k))
(defn ls-set! [k v] (.setItem js/localStorage k v))
(defn ls-del! [k] (.removeItem js/localStorage k))

(defn read-id-token [] (ls-get id-token-key))
(defn read-access-token [] (ls-get access-token-key))

(defn store-tokens!
  [{:keys [id-token access-token]}]
  (when id-token (ls-set! id-token-key id-token))
  (when access-token (ls-set! access-token-key access-token)))

(defn clear-tokens! []
  (ls-del! id-token-key)
  (ls-del! access-token-key))
