(ns advent-2020-clojure.day04-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day04 :refer :all]))

(def TEST_DATA "ecl:gry pid:860033327 eyr:2020 hcl:#fffffd\nbyr:1937 iyr:2017 cid:147 hgt:183cm\n\niyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884\nhcl:#cfa07d byr:1929\n\nhcl:#ae17e1 iyr:2013\neyr:2024\necl:brn pid:760753108 byr:1931\nhgt:179cm\n\nhcl:#cfa07d eyr:2025 pid:166559648\niyr:2011 ecl:brn hgt:59in")
(def PUZZLE_DATA (slurp "resources/day04_data.txt"))


(deftest part1-test
  (is (= 2 (part1 TEST_DATA)))
  (is (= 170 (part1 PUZZLE_DATA))))
