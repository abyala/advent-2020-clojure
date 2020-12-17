(ns advent-2020-clojure.day16-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day16 :refer :all]))

(def TEST_DATA_1 "class: 1-3 or 5-7\nrow: 6-11 or 33-44\nseat: 13-40 or 45-50\n\nyour ticket:\n7,1,14\n\nnearby tickets:\n7,3,47\n40,4,50\n55,2,20\n38,6,12")
(def PUZZLE_DATA (slurp "resources/day16_data.txt"))

(deftest part1-test
  (is (= 71 (part1 TEST_DATA_1)))
  (is (= 29759 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 1307550234719 (part2 PUZZLE_DATA))))

