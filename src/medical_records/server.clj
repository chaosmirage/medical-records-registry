(ns medical-records.server
  (:use 
   [ring.util.response]
   [java-time])
  (:refer-clojure :exclude [range iterate format max min])
  (:require [compojure.handler :as handler]
            [compojure.core :refer [defroutes POST GET PUT DELETE context]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clojure.java.jdbc :as sql]
            [pg-types.all]))

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
  (let [{name :name
         address :address
         gender :gender
         policy_number :policy_number
         birthday :birthday} (assoc patient "id" id)]
    (sql/update! spec :patients
                 {:id (Integer/parseInt id)
                  :name name
                  :address address
                  :gender (seq gender)
                  :policy_number (seq policy_number)
                  :birthday (java-time/local-date "dd.MM.yyyy" birthday)}
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
