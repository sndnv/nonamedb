(ns nonamedb.api.service
  (:use [compojure core])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [nonamedb.storage.engines.memory-engine :refer [get-from put-into]]))

(defn create-service [engine]
  (handler/site
    (defroutes
      api-routes

      (GET "/:key" [key]
        (when-let [result (get-from engine key)]
          {:status       200
           :content-type "application/octet-stream"
           :body         (String. result)}))

      (POST "/:key" [key :as request]
        (if-let [result (put-into engine key (byte-array (map byte (slurp (:body request)))))]
          {:status 200}
          {:status       500
           :content-type "text/plain"
           :body         "Failed to update data"}))

      (PUT "/:key" [key :as request]
        (if-let [result (put-into engine key (byte-array (map byte (slurp (:body request)))))]
          {:status 200}
          {:status       500
           :content-type "text/plain"
           :body         "Failed to update data"}))


      (DELETE "/:key" [key]
        (if-let [result (put-into engine key (byte-array []))]
          {:status 200}
          {:status       500
           :content-type "text/plain"
           :body         "Failed to delete data"}))

      (route/not-found nil))))
