(ns advent-2020-clojure.day25-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day25 :refer :all]))

(def test-card-pub 5764801)
(def test-door-pub 17807724)
(def puzzle-card-pub 6930903)
(def puzzle-door-pub 19716708)

(deftest part1-test
  (testing "Test data"
    (is (= 14897079
           (part1 test-card-pub test-door-pub)
           (part1 test-door-pub test-card-pub))))
  (testing "Puzzle data"
    (is (= 10548634
           (part1 puzzle-card-pub puzzle-door-pub)
           (part1 puzzle-door-pub puzzle-card-pub)))))
