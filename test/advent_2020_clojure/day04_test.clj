(ns advent-2020-clojure.day04-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day04 :refer :all]))

(def TEST_DATA "ecl:gry pid:860033327 eyr:2020 hcl:#fffffd\nbyr:1937 iyr:2017 cid:147 hgt:183cm\n\niyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884\nhcl:#cfa07d byr:1929\n\nhcl:#ae17e1 iyr:2013\neyr:2024\necl:brn pid:760753108 byr:1931\nhgt:179cm\n\nhcl:#cfa07d eyr:2025 pid:166559648\niyr:2011 ecl:brn hgt:59in")
(def PUZZLE_DATA (slurp "resources/day04_data.txt"))

(deftest part1-test
  (is (= 2 (part1 TEST_DATA)))
  (is (= 170 (part1 PUZZLE_DATA))))

(deftest passport-passes-data-integrity-test
  (is (false? (passport-passes-data-integrity? (parse-passport "eyr:1972 cid:100 hcl:#18171d ecl:amb hgt:170 pid:186cm iyr:2018 byr:1926"))))
  (is (false? (passport-passes-data-integrity? (parse-passport "iyr:2019 hcl:#602927 eyr:1967 hgt:170cm ecl:grn pid:012533040 byr:1946"))))
  (is (false? (passport-passes-data-integrity? (parse-passport "hcl:dab227 iyr:2012 ecl:brn hgt:182cm pid:021572410 eyr:2020 byr:1992 cid:277"))))
  (is (false? (passport-passes-data-integrity? (parse-passport "hgt:59cm ecl:zzz eyr:2038 hcl:74454a iyr:2023 pid:3556412378 byr:2007"))))
  (is (passport-passes-data-integrity? (parse-passport "pid:087499704 hgt:74in ecl:grn iyr:2012 eyr:2030 byr:1980 hcl:#623a2f")))
  (is (passport-passes-data-integrity? (parse-passport "eyr:2029 ecl:blu cid:129 byr:1989 iyr:2014 pid:896056539 hcl:#a97842 hgt:165cm")))
  (is (passport-passes-data-integrity? (parse-passport "hcl:#888785 hgt:164cm byr:2001 iyr:2015 cid:88 pid:545766238 ecl:hzl eyr:2022")))
  (is (passport-passes-data-integrity? (parse-passport "iyr:2010 hgt:158cm hcl:#b6652a ecl:blu byr:1944 eyr:2021 pid:093154719"))))

(deftest part2-test
  (is (= 103 (part2 PUZZLE_DATA))))
