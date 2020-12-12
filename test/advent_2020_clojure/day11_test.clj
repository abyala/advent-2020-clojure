(ns advent-2020-clojure.day11-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day11 :refer :all]))

(def TEST_DATA "L.LL.LL.LL\nLLLLLLL.LL\nL.L.L..L..\nLLLL.LL.LL\nL.LL.LL.LL\nL.LLLLL.LL\n..L.L.....\nLLLLLLLLLL\nL.LLLLLL.L\nL.LLLLL.LL")
(def PUZZLE_DATA (slurp "resources/day11_data.txt"))

(deftest part1-test
  (is (= 37 (part1 TEST_DATA)))
  (is (= 2238 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 26 (part2 TEST_DATA)))
  (is (= 2013 (part2 PUZZLE_DATA))))
