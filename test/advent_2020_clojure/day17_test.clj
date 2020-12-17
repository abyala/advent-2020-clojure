(ns advent-2020-clojure.day17-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day17 :refer :all]))

(def TEST_DATA ".#.\n..#\n###")
(def PUZZLE_DATA (slurp "resources/day17_data.txt"))

(deftest num-active-neighbors-test
  (let [actives #{[1 0 0] [2 2 0] [0 2 0] [1 2 0] [2 1 0]}]
    (is (= 1 (num-active-neighbors actives [0 0 0])))
    (is (= 1 (num-active-neighbors actives [1 0 0])))
    (is (= 2 (num-active-neighbors actives [2 0 0])))
    (is (= 3 (num-active-neighbors actives [0 1 0])))
    (is (= 5 (num-active-neighbors actives [1 1 0])))
    (is (= 3 (num-active-neighbors actives [2 1 0])))
    (is (= 1 (num-active-neighbors actives [0 0 1])))
    (is (= 5 (num-active-neighbors actives [1 1 1])))
    (is (= 2 (num-active-neighbors actives [1 0 1])))))

(deftest part1-test
  (is (= 112 (part1 TEST_DATA)))
  (is (= 375 (part1 PUZZLE_DATA))))

#_(deftest part2-test
  (is (= 848 (part2 TEST_DATA)))
  )