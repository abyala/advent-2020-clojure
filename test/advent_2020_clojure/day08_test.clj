(ns advent-2020-clojure.day08-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day08 :refer :all]))

(def TEST_DATA "nop +0\nacc +1\njmp +4\nacc +3\njmp -3\nacc -99\nacc +1\njmp -4\nacc +6")
(def PUZZLE_DATA (slurp "resources/day08_data.txt"))

(deftest part1-test
  (is (= 5 (part1 TEST_DATA)))
  (is (= 1501 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 8 (part2 TEST_DATA)))
  (is (= 509 (part2 PUZZLE_DATA))))