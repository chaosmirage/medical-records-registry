(ns medical-records.server
  (:require [compojure.handler :as handler]
            [compojure.core :refer [defroutes POST GET]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clojure.java.jdbc :as sql])
  (:gen-class))

(def spec "postgresql://localhost:5432/medical-records")

(defn get-all-patients []
  (sql/query spec ["select * from patients order by id"]))

(defn create-patient [patient]
  (sql/insert! spec :patients 
               [:name :address] 
               [(get-in patient ["name"])
                (get-in patient ["address"])]))

(defroutes app-routes
  (GET "/patients" [] (get-all-patients))
  (POST "/patients" {body :body} (create-patient body))
  (GET "/" [] "Hello world!")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body)
      (wrap-json-response)))
