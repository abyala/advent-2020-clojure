(ns advent-2020-clojure.day21-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day21 :refer :all]))

(def TEST_DATA "mxmxvkd kfcds sqjhc nhms (contains dairy, fish)\ntrh fvjkl sbzzf mxmxvkd (contains dairy)\nsqjhc fvjkl (contains soy)\nsqjhc mxmxvkd sbzzf (contains fish)")
(def PUZZLE_DATA (slurp "resources/day21_data.txt"))

(deftest part1-test
  (is (= 5 (part1 TEST_DATA)))
  (is (= 2485 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= "mxmxvkd,sqjhc,fvjkl" (part2 TEST_DATA)))
  (is (= "bqkndvb,zmb,bmrmhm,snhrpv,vflms,bqtvr,qzkjrtl,rkkrx"
         (part2 PUZZLE_DATA))))


