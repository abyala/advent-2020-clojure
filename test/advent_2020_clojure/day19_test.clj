(ns advent-2020-clojure.day19-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day19 :refer :all]))

(def TEST_DATA "0: 4 1 5\n1: 2 3 | 3 2\n2: 4 4 | 5 5\n3: 4 5 | 5 4\n4: \"a\"\n5: \"b\"\n\nababbb\nbababa\nabbbab\naaabbb\naaaabbb")
(def PUZZLE_DATA (slurp "resources/day19_data.txt"))

(deftest part1-test
  (is (= 2 (part1 TEST_DATA)))
  (is (= 109 (part1 PUZZLE_DATA))))
