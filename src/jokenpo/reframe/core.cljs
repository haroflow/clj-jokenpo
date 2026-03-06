(ns jokenpo.reframe.core
  (:require
   [jokenpo.logic :as logic]
   [re-frame.core :as rf]
   [reagent.dom.client :as rdomc]))

(def log-interceptor
  (rf/->interceptor
   :id      :my-logger
   :after (fn [context]
            (println "ev:" (get-in context [:coeffects :event])
                     "db:" (get-in context [:effects :db]))
            context)))

(rf/reg-global-interceptor log-interceptor)

(rf/reg-fx
 :timeout
 (fn [{:keys [ms event]}]
   (js/setTimeout #(rf/dispatch event) ms)))

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:text "Choose!"
    :player-choice nil
    :computer-choice nil
    :score {:player 0 :computer 0}}))

(rf/reg-event-db
 :next-round
 (fn [db _]
   (assoc db
          :text "Choose!"
          :player-choice nil
          :computer-choice nil)))

(rf/reg-event-fx
 :player-selected-option
 (fn [{:keys [db]} [_ opcao-keyword]]
   {:db (assoc db
               :player-choice opcao-keyword
               :text "...")
    :dispatch [:animate-computer-choice 0 (+ 25 (rand-int 25))]}))

(rf/reg-event-fx
 :check-winner
 (fn [{:keys [db]} _event]
   (let [result (logic/get-winner (:player-choice db) (:computer-choice db))
         score (logic/calculate-score result (:score db))
         text (logic/get-round-result-text result)]
     {:db (assoc db :text text :score score)
      :timeout {:ms 2000 :event [:next-round]}})))

(rf/reg-event-fx
 :animate-computer-choice
 (fn [{:keys [db]} [_ current-frame max-frames]]
   (let [option (get {0 :rock 1 :paper 2 :scissors} (mod current-frame 3))]
     (if (>= current-frame max-frames)
       {:db (assoc db :computer-choice option)
        :dispatch [:check-winner]}
       {:db (assoc db :computer-choice option)
        :timeout {:ms (+ 50 (* 3 current-frame))
                  :event [:animate-computer-choice (inc current-frame) max-frames]}}))))

(defn player-option [selected option-kw]
  [:div.option
   {:on-click #(rf/dispatch [:player-selected-option option-kw])
    :class [(when (nil? selected) "clickable")
            (when (= option-kw selected) "selected")]}
   [:img {:src (str "/imgs/" (name option-kw) ".png")}]])

(rf/reg-sub :text (fn [db _] (:text db)))
(rf/reg-sub :player-choice (fn [db _] (:player-choice db)))
(rf/reg-sub :computer-choice (fn [db _] (:computer-choice db)))
(rf/reg-sub :score (fn [db _] (logic/describe-score (:score db))))

(defn root-el []
  (let [text (rf/subscribe [:text])
        player-choice (rf/subscribe [:player-choice])
        computer-choice (rf/subscribe [:computer-choice])
        score (rf/subscribe [:score])]
    (fn []
      [:div.container
       [:div.options
        [:div#computer.option
         (case @computer-choice
           :rock [:img {:src "/imgs/rock.png" :class "visible"}]
           :paper [:img {:src "/imgs/paper.png" :class "visible"}]
           :scissors [:img {:src "/imgs/scissors.png" :class "visible"}]
           nil)]]
       [:h1 @text]
       [:h4 @score]
       [:div.options
        [player-option @player-choice :rock]
        [player-option @player-choice :paper]
        [player-option @player-choice :scissors]]])))

(defonce root (delay (rdomc/create-root (.getElementById js/document "root"))))

(defn ^:export ^:dev/after-load init []
  (println "reframe")
  (rf/dispatch-sync [:initialize])
  (rdomc/render @root [root-el]))