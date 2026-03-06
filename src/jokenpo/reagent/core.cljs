(ns jokenpo.reagent.core
  (:require
   [clojure.core.async :refer [<! go timeout]]
   [reagent.core :as r]
   [reagent.dom.client :as rdomc]
   [jokenpo.logic :as logic]))

(defn execute-round [state]
  (go
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

    (swap! state assoc
           :text "Choose!"
           :player-choice nil
           :computer-choice nil)))

(defn player-selected-option [state option]
  (swap! state assoc
         :player-choice option
         :text "...")
  (execute-round state))

(defn player-option [*state option-kw]
  [:div.option
   {:on-click #(player-selected-option *state option-kw)
    :class [(when (nil? (:player-choice @*state)) "clickable")
            (when (= option-kw (:player-choice @*state)) "selected")]}
   [:img {:src (str "/imgs/" (name option-kw) ".png")}]])

(defn root-el []
  (let [state (r/atom {:text "Choose!"
                       :score {:player 0 :computer 0}})]
    (fn []
      (println "state:" (pr-str @state))
      [:div.container
       [:div.options 
        [:div#computer.option
         (case (:computer-choice @state)
           :rock [:img {:src "/imgs/rock.png" :class "visible"}]
           :paper [:img {:src "/imgs/paper.png" :class "visible"}]
           :scissors [:img {:src "/imgs/scissors.png" :class "visible"}]
           nil)]]
       [:h1 (:text @state)]
       [:h4 (logic/describe-score (:score @state))]
       [:div.options
        [player-option state :rock]
        [player-option state :paper]
        [player-option state :scissors]]])))

(defonce root (delay (rdomc/create-root (.getElementById js/document "root"))))

(defn ^:export ^:dev/after-load init []
  (println "reagent")
  (rdomc/render @root [root-el]))