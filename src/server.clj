(ns server
  (:require [ring.adapter.jetty :as jetty]))

(defn handle-request [request] 
  (println request)
  {
   :status 200, 
   :headers {"Content-Type" "text/plain"}
   :body "Hello World\n"})

(jetty/run-jetty
 handle-request
 {
  :host "127.0.0.1"
  :port 8081
  :join? false})

(defn -main [] 
  (println "Server running at http://127.0.0.1:8081/"))
