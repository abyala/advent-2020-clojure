(ns advent-2020-clojure.day15-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day15 :refer :all]))

(def TEST_DATA "0,3,6")
(def PUZZLE_DATA "20,0,1,11,6,3")

(deftest part1-test
  (is (= 436 (part1 TEST_DATA)))
  (is (= 421 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 175594 (part2 TEST_DATA)))
  (is (= 436 (part2 PUZZLE_DATA))))
