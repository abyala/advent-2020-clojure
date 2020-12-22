(ns advent-2020-clojure.day22-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day22 :refer :all]))

(def TEST_DATA "Player 1:\n9\n2\n6\n3\n1\n\nPlayer 2:\n5\n8\n4\n7\n10")
(def PUZZLE_DATA (slurp "resources/day22_data.txt"))

(deftest part1-test
  (is (= 306 (part1 TEST_DATA)))
  (is (= 32366 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 291 (part2 TEST_DATA)))
  (is (= 30891 (part2 PUZZLE_DATA))))
