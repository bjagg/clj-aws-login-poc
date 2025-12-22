(ns aws-login.webapp.jwt)

(defn ^:private b64url->b64 [s]
  (-> s
      (.replace "-" "+")
      (.replace "_" "/")))

(defn decode-jwt-payload
  "Decodes the JSON payload of a JWT. Returns a JS object (or nil on failure)."
  [jwt]
  (try
    (when (and jwt (string? jwt))
      (let [parts (.split jwt ".")
            payload (aget parts 1)]
        (when payload
          (let [json (.atob js/window (b64url->b64 payload))]
            (.parse js/JSON json)))))
    (catch :default _ nil)))
