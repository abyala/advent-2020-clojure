(ns advent-2020-clojure.day13-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day13 :refer :all]))

(def TEST_DATA "939\n7,13,x,x,59,x,31,19")
(def PUZZLE_DATA (slurp "resources/day13_data.txt"))

(deftest part1-test
  (is (= 295  (part1 TEST_DATA)))
  (is (= 261  (part1 PUZZLE_DATA))))

(println "Day 13 part 2 is an irritating Chinese Remainder Theorem
 problem. Plug the following into wolframalpha.com:")
(println (part2 PUZZLE_DATA))

; ANSWER: 807435693182510
