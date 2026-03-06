(ns jokenpo.static.core
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [hiccup2.core :as h]
   [jokenpo.logic :as logic]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.flash :refer [wrap-flash]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.session :refer [wrap-session]]))

(defn html5 [& body]
  (str
   (h/html
    (h/raw "<!DOCTYPE html>")
    [:html {:lang "en"}
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:title "Jokenpo"]
      [:link {:rel "stylesheet" :type "text/css" :href "/css/styles.css"}]]
     [:body body]])))

(defn computer-option [img visible?]
  [:img {:src img
         :class [(when visible? "visible")]}])

(defn player-option [img value selected?]
  [:button.option.clickable
   {:type "submit" :name "player-choice" :value value
    :class [(when selected? "selected")]}
   [:img {:src img}]])

(defn main-page [player-choice computer-choice result text score]
  (html5
   [:div.container
    [:div.options
     [:div#computer.option
      (computer-option "/imgs/rock.png" (= :rock computer-choice))
      (computer-option "/imgs/paper.png" (= :paper computer-choice))
      (computer-option "/imgs/scissors.png" (= :scissors computer-choice))]]
    (if result
      [:h1 text]
      [:h1 "Choose!"])
    [:div#scoreboard
     [:h4 (logic/describe-score score)]
     [:form#reset-form {:method "POST" :action "/reset"}
      [:button "Reset Score"]]]
    [:form#game-form {:method "POST" :action "/"}
     [:div.options
      (player-option "/imgs/rock.png" "rock" (= :rock player-choice))
      (player-option "/imgs/paper.png" "paper" (= :paper player-choice))
      (player-option "/imgs/scissors.png" "scissors" (= :scissors player-choice))]]]))

(defn play-handler [req]
  (let [form-params (:form-params req)
        player-choice (keyword (form-params "player-choice"))
        computer-choice (logic/choose-random)
        result (logic/get-winner player-choice computer-choice)
        text (logic/get-round-result-text result)
        current-session (:session req {})
        score (get current-session :score {:player 0 :computer 0})
        new-score (logic/calculate-score result score)]
    ;; Redirect to avoid double POSTing when refreshing the page
    {:status 303
     :headers {"Location" "/"}
     :flash {:player-choice player-choice
             :computer-choice computer-choice
             :result result
             :text text}
     :session (assoc current-session :score new-score)}))

(defn reset-score-handler [req]
  (let [current-session (:session req {})]
    ;; Redirect to avoid double POSTing when refreshing the page
    {:status 303
     :headers {"Location" "/"}
     :session (assoc current-session :score {:player 0 :computer 0})}))

(defn index-handler [req]
  (let [{:keys [player-choice computer-choice result text]} (:flash req)
        score (get-in req [:session :score] {:player 0 :computer 0})]
    (main-page player-choice computer-choice result text score)))

(defroutes app-routes
  (GET "/" req (index-handler req))
  (POST "/" req (play-handler req))
  (POST "/reset" req (reset-score-handler req))
  (route/not-found "<h1>Page not found</h1>"))

(def app
  (-> app-routes
      wrap-params
      (wrap-resource "public_server")
      wrap-content-type
      wrap-flash
      wrap-session))

(defn -main []
  (println "Starting server on port 3000")
  (jetty/run-jetty app {:port 3000}))

(defonce dev-server (atom nil))
(defn start-dev-server! []
  (when @dev-server
    (.stop @dev-server))

  (let [server (jetty/run-jetty #'app {:port 3000 :join? false})]
    (reset! dev-server server)))

(comment
  (start-dev-server!)
  :rcf)