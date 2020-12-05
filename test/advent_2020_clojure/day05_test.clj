(ns advent-2020-clojure.day05-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day05 :refer :all]))

(def TEST_DATA "")
(def PUZZLE_DATA (slurp "resources/day05_data.txt"))

; PART 1: 818
(deftest part1-test
  (is (= 818 (part1 PUZZLE_DATA))))