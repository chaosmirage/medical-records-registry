(ns medical-records.server
  (:use [ring.util.response])
  (:require [compojure.handler :as handler]
            [compojure.core :refer [defroutes POST GET PUT DELETE context]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clojure.java.jdbc :as sql]))

(def spec {:dbtype "postgres"
           :dbname "medical-records"})

(defn get-all-patients []
  (sql/query spec ["select * from patients order by id"]))

(defn get-patient [id]
  (let [results (sql/query spec ["select * from patients where id = cast(? as integer)" id])]
    (cond
      (empty? results) {:status 404}
      :else (response (first results)))))

(defn create-patient [patient]
  (sql/insert! spec :patients 
               [:name :address] 
               [(get-in patient ["name"])
                (get-in patient ["address"])]))

(defn update-patient [id patient]
  (println patient)
  (let [{name :name
         address :address
         gender :gender} (assoc patient "id" id)]
    (println name)
    (sql/update! spec :patients
                 {:id (Integer/parseInt id)
                  :name name
                  :address address
                  :gender gender}
                 ["id = cast(? as integer)" id]))
  (get-patient id))

(defn delete-patient [id] 
  (sql/delete! spec :patients ["id = cast(? as integer)" id])
  {:status 204})

(defroutes app-routes
  (context "/patients" [] (defroutes patients-routes
                            (GET "/" [] (get-all-patients))
                            (POST "/" {body :body} (create-patient body))
                            (context "/:id" [id] (defroutes patients-routes
                                                   (GET "/" [] (get-patient id))
                                                   (PUT "/" {body :body} (update-patient id body))
                                                   (DELETE "/" [] (delete-patient id)))))
  
  (GET "/" [] "Hello world!")
  (route/not-found "Not Found")))

(def app
  (-> (handler/api app-routes)
      (wrap-json-body {:keywords? true})
      (wrap-json-response)))
