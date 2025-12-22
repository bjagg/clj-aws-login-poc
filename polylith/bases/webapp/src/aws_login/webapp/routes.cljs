(ns aws-login.webapp.routes)

(defn pathname [] (.-pathname js/location))
(defn search [] (.-search js/location))

(defn route []
  (if (= (pathname) "/callback") :callback :home))

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

(defn init! []
  (route))
