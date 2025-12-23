(ns aws-login.oauth-pkce.impl
  (:require [clojure.string :as str]
            [aws-login.oauth-pkce.oauth-storage :as store]))

(defn ^:private base64url-encode-bytes [^js/Uint8Array bytes]
  ;; JS equivalent: btoa(String.fromCharCode.apply(null, Array.from(bytes)))
  (let [arr (js/Array.from bytes)
        s   (.apply (.-fromCharCode js/String) nil arr)
        b64 (.btoa js/window s)]
    (-> b64
        (.replace #"\+" "-")
        (.replace #"/" "_")
        (.replace #"=+$" ""))))

(defn ^:private random-verifier
  ([] (random-verifier 64))
  ([len]
   (let [bytes (js/Uint8Array. len)]
     (.getRandomValues (.-crypto js/window) bytes)
     (base64url-encode-bytes bytes))))

(defn ^:private sha256-bytes [^js/Uint8Array bytes]
  (-> (.digest (.-subtle (.-crypto js/window)) "SHA-256" bytes)
      (.then (fn [buf] (js/Uint8Array. buf)))))

(defn ^:private challenge-from-verifier [verifier]
  (let [bytes (.encode (js/TextEncoder.) verifier)]
    (-> (sha256-bytes bytes)
        (.then base64url-encode-bytes))))

(defn authorize-url
  [{:keys [cognito-domain client-id redirect-uri scopes]} code-challenge]
  (let [scope (-> (or scopes "openid email profile")
                  (str/trim)
                  (str/replace #"\s+" " "))
        qs (doto (js/URLSearchParams.)
             (.set "client_id" client-id)
             (.set "response_type" "code")
             (.set "redirect_uri" redirect-uri)
             (.set "scope" scope)
             (.set "code_challenge" code-challenge)
             (.set "code_challenge_method" "S256"))]
    (str cognito-domain "/oauth2/authorize?" (.toString qs))))

(defn logout-url
  [{:keys [cognito-domain client-id logout-uri]}]
  (let [qs (doto (js/URLSearchParams.)
             (.set "client_id" client-id)
             (.set "logout_uri" logout-uri))]
    (str cognito-domain "/logout?" (.toString qs))))

(defn start-login!
  "Generates verifier+challenge, stores verifier in sessionStorage, and navigates to Hosted UI authorize."
  [cfg]
  (let [verifier (random-verifier)]
    (store/store-verifier! verifier)
    (-> (challenge-from-verifier verifier)
        (.then (fn [challenge]
                 (set! (.-location js/window) (authorize-url cfg challenge)))))))

(defn exchange-code!
  "POSTs to /oauth2/token with code_verifier. Returns a JS Promise of the token JSON."
  [{:keys [cognito-domain client-id redirect-uri]} code]
  (let [verifier (store/read-verifier)]
    (when-not verifier
      (throw (js/Error. "Missing PKCE verifier in sessionStorage.")))
    (let [body (doto (js/URLSearchParams.)
                 (.set "grant_type" "authorization_code")
                 (.set "client_id" client-id)
                 (.set "code" code)
                 (.set "redirect_uri" redirect-uri)
                 (.set "code_verifier" verifier))]
      (-> (js/fetch (str cognito-domain "/oauth2/token")
                    #js {:method "POST"
                         :headers #js {"Content-Type" "application/x-www-form-urlencoded"}
                         :body (.toString body)})
          (.then (fn [resp]
                   (if (.-ok resp)
                     (.json resp)
                     (-> (.text resp)
                         (.then (fn [t]
                                  (throw (js/Error.
                                          (str "Token exchange failed (" (.-status resp) "):\n" t)))))))))))))

(defn refresh-tokens!
  "Refreshes tokens using the stored refresh token.
       Returns a JS Promise of the token JSON."
  [{:keys [cognito-domain client-id]} refresh-token]
  (let [body (doto (js/URLSearchParams.)
               (.set "grant_type" "refresh_token")
               (.set "client_id" client-id)
               (.set "refresh_token" refresh-token))]
    (-> (js/fetch (str cognito-domain "/oauth2/token")
                  #js {:method "POST"
                       :headers #js {"Content-Type" "application/x-www-form-urlencoded"}
                       :body (.toString body)})
        (.then (fn [resp]
                 (if (.-ok resp)
                   (.json resp)
                   (-> (.text resp)
                       (.then (fn [t]
                                (throw (js/Error.
                                        (str "Token refresh failed (" (.-status resp) "):\n" t))))))))))))
