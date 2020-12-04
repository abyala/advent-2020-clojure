(ns advent-2020-clojure.day04
  (:require [clojure.string :as str]))

(defn in-range? [min max s]
  (when-let [i (try (Integer/parseInt s)
                    (catch NumberFormatException _ nil))]
    (<= min i max)))

(defn birth-year? [s] (in-range? 1920 2002 s))
(defn issue-year? [s] (in-range? 2010 2020 s))
(defn expiration-year? [s] (in-range? 2020 2030 s))
(defn height? [s] (when-let [[_ amt unit] (re-matches #"(\d+)(in|cm)" s)]
                    (case unit
                      "cm" (in-range? 150 193 amt)
                      "in" (in-range? 59 76 amt))))
(defn hair-color? [s] (re-matches #"\#[0-9a-f]{6}" s))
(defn eye-color? [s] (#{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"} s))
(defn passport-id? [s] (re-matches #"[\d]{9}" s))

(def passport-fields {"byr" birth-year?
                      "iyr" issue-year?
                      "eyr" expiration-year?
                      "hgt" height?
                      "hcl" hair-color?
                      "ecl" eye-color?
                      "pid" passport-id?})

(defn parse-passport
  "Reads a line of text of form \"key1:value1 key2:value2\" and returns a map of each key to its value."
  [line]
  (->> (str/split line #"[\n ]")
       (map #(str/split % #":"))
       (into {})))

(defn parse-input
  "Reads an entire input string and returns a sequence of passports, using blank lines as passport delimiters."
  [input]
  (as-> (str/replace input "\r" "") x
        (str/split x #"\n\n")
        (map parse-passport x)))

(defn has-required-fields?
  "Returns true if the passport contains all required fields."
  [passport]
  (->> (keys passport-fields)
       (map #(passport %))
       (every? some?)))

(defn field-passes-data-integrity?
  "Returns true if the passport field, expressed as [k v], does not fail a data integrity check."
  [[k v]]
  (let [check (or (passport-fields k)
                  any?)]
    (check v)))

(defn passport-passes-data-integrity?
  "Returns true if none of the fields within the passport fail a data integrity check."
  [passport]
  (every? field-passes-data-integrity? passport))

(defn valid-passport?
  "Returns true if a passport passes all of the validation checks."
  [passport validations]
  (reduce (fn [acc check] (and acc (check passport)))
          true
          validations))

(defn num-valid-passports
  "Returns the number of passports that pass all of the required validation checks."
  [input validations]
  (->> (parse-input input)
       (filter (fn [passport] (valid-passport? passport validations)))
       count))

(defn part1 [input]
  (num-valid-passports input [has-required-fields?]))
(defn part2 [input]
  (num-valid-passports input [has-required-fields? passport-passes-data-integrity?]))