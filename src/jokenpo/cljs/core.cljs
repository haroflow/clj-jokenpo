(ns jokenpo.cljs.core
  #_{:clj-kondo/ignore [:unused-namespace]}
  (:require
   [clojure.core.async :refer [<! go timeout]]
   [jokenpo.cljs.html :as html]
   [jokenpo.logic :as logic]
   ;; hiccups.runtime is "unused", but without it I had some errors when adding (logic/describe-score ...) to the page function...
   [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(def state (atom {:player-choice nil
                  :computer-choice nil
                  :text "Choose!"
                  :score {:player 0 :computer 0}}))

(defn render [{:keys [player-choice computer-choice text score]}]
  (html/conditional-class "rock" "selected" (= :rock player-choice))
  (html/conditional-class "paper" "selected" (= :paper player-choice))
  (html/conditional-class "scissors" "selected" (= :scissors player-choice))
  
  (html/conditional-class "computer-rock" "visible" (= :rock computer-choice))
  (html/conditional-class "computer-paper" "visible" (= :paper computer-choice))
  (html/conditional-class "computer-scissors" "visible" (= :scissors computer-choice))
  
  (html/set-inner-text "text" text)
  (html/set-inner-text "score" (logic/describe-score score)))

(add-watch state ::a
           (fn [_ _ _ new-state]
             (println "state:" new-state)
             (render new-state)))

(defn execute-round []
  (go
    (html/remove-class "rock" "clickable")
    (html/remove-class "paper" "clickable")
    (html/remove-class "scissors" "clickable")

    ;; Computer choice animation
    ;; Probably not mathematically fair, but looks great.
    (dotimes [i (+ 25 (rand-int 25))]
      (swap! state assoc :computer-choice
             (get {0 :rock 1 :paper 2 :scissors} (mod i 3)))
      (<! (timeout (+ 50 (* 3 i)))))

    (let [result (logic/get-winner (:player-choice @state) (:computer-choice @state))
          score (logic/calculate-score result (:score @state))
          text (logic/get-round-result-text result)]
      (swap! state assoc :text text :score score))

    (<! (timeout 2000))

    (html/add-class "rock" "clickable")
    (html/add-class "paper" "clickable")
    (html/add-class "scissors" "clickable")
    (swap! state assoc
           :text "Choose!"
           :player-choice nil
           :computer-choice nil)))

(defn player-selected-option [option]
  (swap! state assoc
         :player-choice option
         :text "...")
  (execute-round))

(defn page []
  (html
   [:div.container
    [:div.options
     [:div#computer.option
      [:img#computer-rock {:src "/imgs/rock.png"}]
      [:img#computer-paper {:src "/imgs/paper.png"}]
      [:img#computer-scissors {:src "/imgs/scissors.png"}]]]
    [:h1#text "Choose!"]
    [:h4#score (logic/describe-score (:score @state))]
    [:div.options
     [:div#rock.option.clickable [:img {:src "/imgs/rock.png"}]]
     [:div#paper.option.clickable [:img {:src "/imgs/paper.png"}]]
     [:div#scissors.option.clickable [:img {:src "/imgs/scissors.png"}]]]]))

(defn ^:export ^:dev/after-load init []
  (println "cljs")
  (set! (.-innerHTML (js/document.getElementById "root")) (page))
  (html/add-event-listener "rock" "click" #(player-selected-option :rock))
  (html/add-event-listener "paper" "click" #(player-selected-option :paper))
  (html/add-event-listener "scissors" "click" #(player-selected-option :scissors)))