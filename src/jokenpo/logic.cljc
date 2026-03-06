(ns jokenpo.logic)

(def defeats-who
  {:rock :scissors
   :paper :rock
   :scissors :paper})

(defn get-winner [player computer]
  (cond
    (or (nil? player) (nil? computer)) (throw (ex-info "Nil arguments" {:player player :computer computer}))
    (= player computer) :draw
    (= (defeats-who player) computer) :player
    :else :computer))

(defn choose-random []
  (rand-nth [:rock :paper :scissors]))

(defn get-round-result-text [result]
  (case result
    :draw "It's a draw!"
    :player "You win!"
    :computer "You lose!"))

(defn describe-score [{:keys [player computer]}]
  (str "Player: " player ", Computer: " computer))

(defn calculate-score [result score]
  (case result
    :player (update score :player inc)
    :computer (update score :computer inc)
    :draw score))