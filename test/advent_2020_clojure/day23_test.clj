(ns advent-2020-clojure.day23-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day23 :refer :all]))

(def TEST_DATA "389125467")
(def PUZZLE_DATA "487912365")

(deftest part1-test
  (is (= "92658374" (part1 TEST_DATA 10)))
  (is (= "67384529" (part1 TEST_DATA 100)))
  (is (= "89573246" (part1 PUZZLE_DATA 100))))

(deftest extend-cups-test
  (is (= {:current 1 :size 6 :cups {1 2, 2 3, 3 4, 4 5, 5 6, 6 1}}
         (extend-cups (init-cups "12345") 6))))

(deftest part2-test
  (is (= 2029056128 (part2 PUZZLE_DATA 1000000 10000000))))