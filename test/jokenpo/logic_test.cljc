(ns jokenpo.logic-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [jokenpo.logic :as logic]) 
  (:import
   [java.lang Exception]))

(deftest test--get-winner
  (testing "Draw"
    (is (= :draw (logic/get-winner :rock :rock)))
    (is (= :draw (logic/get-winner :paper :paper)))
    (is (= :draw (logic/get-winner :scissors :scissors))))
  
  (testing "Player wins"
    (is (= :player (logic/get-winner :rock :scissors)))
    (is (= :player (logic/get-winner :paper :rock)))
    (is (= :player (logic/get-winner :scissors :paper))))

  (testing "Computer wins"
    (is (= :computer (logic/get-winner :rock :paper)))
    (is (= :computer (logic/get-winner :paper :scissors)))
    (is (= :computer (logic/get-winner :scissors :rock))))

  (testing "Nil arguments should throw"
    (is (thrown? Exception (logic/get-winner :rock nil)))
    (is (thrown? Exception (logic/get-winner nil :rock)))))

(deftest test--get-round-result-text
  (testing "Should return string for valid results"
    (is (string? (logic/get-round-result-text :draw)))
    (is (string? (logic/get-round-result-text :player)))
    (is (string? (logic/get-round-result-text :computer))))
  
  (testing "Should throw on invalid result"
    (is (thrown? Exception (logic/get-round-result-text :blah)))
    (is (thrown? Exception (logic/get-round-result-text nil)))
    (is (thrown? Exception (logic/get-round-result-text 0)))
    (is (thrown? Exception (logic/get-round-result-text "draw")))))

(deftest test--calculate-score
  (testing "Draw"
    (is (= {:player 0 :computer 0} (logic/calculate-score :draw {:player 0 :computer 0})))
    (is (= {:player 0 :computer 1} (logic/calculate-score :draw {:player 0 :computer 1})))
    (is (= {:player 1 :computer 0} (logic/calculate-score :draw {:player 1 :computer 0}))))

  (testing "Player wins"
    (is (= {:player 2 :computer 1} (logic/calculate-score :player {:player 1 :computer 1})))
    (is (= {:player 1 :computer 0} (logic/calculate-score :player {:player 0 :computer 0}))))

  (testing "Computer wins"
    (is (= {:player 0 :computer 1} (logic/calculate-score :computer {:player 0 :computer 0})))
    (is (= {:player 1 :computer 1} (logic/calculate-score :computer {:player 1 :computer 0})))))