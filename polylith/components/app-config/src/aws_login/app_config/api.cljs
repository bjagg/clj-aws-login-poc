(ns aws-login.app-config.api
  "Stable public API for runtime config."
  (:require [aws-login.app-config.impl :as impl]))

(defn get-config [] (impl/get-config))
