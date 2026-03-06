(ns jokenpo.cli.core
  (:gen-class)
  (:require
   [jokenpo.logic :as logic]))

(defn read-option! []
  (let [opcao (read-line)]
    (case opcao
      "r" :rock
      "p" :paper
      "s" :scissors
      "q" :quit
      :invalid)))

(defn execute! [score]
  (println "\nChoose: (r)ock, (p)aper, (s)cissors or (q)uit")

  (let [player-choice (read-option!)]
    (case player-choice
      :quit
      (println "Goodbye! Final score:" (logic/describe-score score))

      :invalid
      (do (println "Invalid choice...")
          (recur score))

      (let [computer-choice (logic/choose-random)
            who-won (logic/get-winner player-choice computer-choice)
            new-score (logic/calculate-score who-won score)]
        (println "You chose" (name player-choice))
        (println "Computer chose" (name computer-choice))
        (case who-won
          :player (do (println "You won, congratulations!")
                      (println "The new score is" (logic/describe-score new-score)))
          :computer (do (println "Computer won!")
                        (println "The new score is" (logic/describe-score new-score)))
          :draw (println "It's a draw!"))
        (recur new-score)))))

(comment
  (execute! {:player 0 :computer 0})
  :rcf)

(defn -main [& _args]
  (println "# Rock Paper Scissors")
  (execute! {:player 0 :computer 0}))