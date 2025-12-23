(ns aws-login.webapp.routes)

;; This project intentionally avoids routing libraries to keep the PoC minimal.
;; We use:
;; - path-based routing only for /callback (Cognito redirect target)
;; - hash routing for in-app navigation (/#/, /#/claims)

(defn pathname [] (.-pathname js/location))
(defn search [] (.-search js/location))
(defn hash-fragment [] (.-hash js/location)) ;; includes leading '#', or "".

(defn- normalize-hash-path [h]
  (cond
    (or (nil? h) (= h "") (= h "#") (= h "#/")) "/"
    (.startsWith h "#/") (.substring h 1) ;; "#/claims" -> "/claims"
    :else "/"))

(defn- hash-path []
  (normalize-hash-path (hash-fragment)))

(defn page
  "Returns a keyword page for the current URL."
  []
  (if (= (pathname) "/callback")
    :callback
    (case (hash-path)
      "/claims" :claims
      :home)))

(defn query-params []
  (let [qs (js/URLSearchParams. (search))
        obj (js/Object.)]
    (.forEach qs (fn [v k] (aset obj k v)))
    obj))

(defn oauth-code []
  (let [p (query-params)]
    (or (aget p "code") nil)))

(defn oauth-error []
  (let [p (query-params)]
    (or (aget p "error") nil)))

(defn replace-state! [path]
  (.replaceState js/history #js {} "" path))

(defn navigate!
  "Navigate within the SPA. Uses hash routing."
  [path]
  ;; Accept "/" or "/claims"
  (set! (.-hash js/location) (str "#" path)))

(defn current-route []
  {:page (page)
   :path (hash-path)
   :pathname (pathname)})

(defn start!
  "Starts a hashchange listener and invokes `on-change` with the current route.
   `on-change` receives a route map (see `current-route`). Returns a stop fn."
  [on-change]
  (let [handler (fn [_] (on-change (current-route)))]
    (.addEventListener js/window "hashchange" handler)
    ;; Emit initial route
    (on-change (current-route))
    (fn stop! []
      (.removeEventListener js/window "hashchange" handler))))
