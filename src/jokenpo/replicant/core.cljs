(ns jokenpo.replicant.core
  (:require
   [jokenpo.logic :as logic]
   [replicant.dom :as r]))

(defonce store
  (atom {:text "Choose!"
         :player-choice nil
         :computer-choice nil
         :score {:player 0 :computer 0}}))

(defn player-option [state option-kw]
  [:div.option
   {:on {:click [[::player-selected-option option-kw]]}
    :class [(when (nil? (:player-choice state)) "clickable")
            (when (= option-kw (:player-choice state)) "selected")]}
   [:img {:src (str "/imgs/" (name option-kw) ".png")}]])

(defn main-page [state]
  [:div.container
   [:div.options
    [:div#computer.option
     (case (:computer-choice state)
       :rock [:img {:src "/imgs/rock.png" :class "visible"}]
       :paper [:img {:src "/imgs/paper.png" :class "visible"}]
       :scissors [:img {:src "/imgs/scissors.png" :class "visible"}]
       nil)]]
   [:h1 (:text state)]
   [:h4 (logic/describe-score (:score state))]
   [:div.options
    (player-option state :rock)
    (player-option state :paper)
    (player-option state :scissors)]])

(defn process-action [state _replicant-data [action & args]]
  (println "process-action" action args)
  (case action
    :action/assoc
    [(into [:effect/assoc] args)]

    ::check-winner
    (let [player-choice (:player-choice state)
          computer-choice (:computer-choice state)
          result (logic/get-winner player-choice computer-choice)
          text (logic/get-round-result-text result)
          score (logic/calculate-score result (:score @store))]
      [[:effect/assoc :text text :score score]
       [:effect/timeout 2000
        [[:action/assoc :text "Choose!" :player-choice nil :computer-choice nil]]]])

    ::animate-computer-choice
    (let [[current-frame max-frames] args]
      (if (>= current-frame max-frames)
        [[:effect/dispatch [[::check-winner]]]]
        (let [computer-choice (get {0 :rock 1 :paper 2 :scissors} (mod current-frame 3))]
          [[:effect/assoc :computer-choice computer-choice]
           [:effect/timeout (+ 50 (* 3 current-frame))
            [[::animate-computer-choice (inc current-frame) max-frames]]]])))

    ::player-selected-option
    (let [[player-choice] args]
      [[:effect/assoc :player-choice player-choice :text "..."]
       [:effect/start-animation]])))

(defn process-effect! [store handle-actions effect-data]
  (let [[effect & args] effect-data]
    (println "process-effect!" effect args)
    (case effect
      :effect/assoc
      (apply swap! store assoc args)

      :effect/timeout
      (let [[millis actions] args]
        (js/setTimeout #(handle-actions actions) millis))
      
      :effect/dispatch
      (let [[actions] args]
        (handle-actions actions))
      
      :effect/start-animation
      (let [max-frames (+ 25 (rand-int 25))]
        (handle-actions [[::animate-computer-choice 0 max-frames]]))

      (js/console.error "Unknown effect" (clj->js effect-data)))))

(defn handle-actions! [store replicant-data event-data]
  (let [handle-actions!* (partial handle-actions! store replicant-data)]
    (->> (mapcat #(process-action @store replicant-data %) event-data)
         (run! #(process-effect! store handle-actions!* %)))))

(defn init-ui [store]
  (add-watch store ::render
             (fn [_ _ _ new-state]
               (r/render js/document.body (main-page new-state))))

  (r/set-dispatch!
   (fn [replicant-data event-data]
     (handle-actions! store replicant-data event-data)))

  (swap! store assoc :started-at (.getTime (js/Date.))))

(defn ^:export init []
  (println "init")
  (init-ui store))

(defn ^:export ^:dev/after-load reload []
  (println "reload")
  (init-ui store))



