(ns medical-records.server
  (:require [ring.adapter.jetty :as jetty])
  (:require [compojure.core :refer [defroutes POST GET]]))

(defroutes routes
  (GET "/" [] "Hello world")
  (POST "/patients" [request] "created"))

(jetty/run-jetty
 #'routes
 {:host "127.0.0.1"
  :port 8081
  :join? false})

(defn -main []
  (println "Server running at http://127.0.0.1:8081/"))
