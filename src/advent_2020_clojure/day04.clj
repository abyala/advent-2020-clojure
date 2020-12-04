(ns advent-2020-clojure.day04
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def required-fields #{"byr" "iyr" "eyr" "hgt"
                       "hcl" "ecl" "pid"})

(defn parse-passport [line]
  (->> (str/split line #"[:\ ]")
       (partition 2)
       (map vec)
       (into {})))

(defn parse-input [input]
  (->> (str/split-lines input)
       (partition-by (partial = ""))
       (filter (partial not= '("")))
       (map (fn [words] (str/join " " words)))
       (map parse-passport)))

(defn valid-passport? [passport]
  (->> (keys passport)
       set
       (set/difference required-fields)
       empty?))

(defn maybe-parse-int [s]
  (try (Integer/parseInt s)
       (catch NumberFormatException _ nil)))

(defn in-range? [min max s]
  (when-let [i (maybe-parse-int s)]
    (<= min i max)))

(defn birth-year? [s] (in-range? 1920 2002 s))
(defn issue-year? [s] (in-range? 2010 2020 s))
(defn expiration-year? [s] (in-range? 2020 2030 s))
(defn height? [s] (when-let [[_ amt unit] (re-matches #"(\d+)(in|cm)" s)]
                    (case unit
                      "cm" (<= 150 (Integer/parseInt amt) 193)
                      "in" (<= 59 (Integer/parseInt amt) 76))))
(defn hair-color? [s] (some? (re-matches #"\#[0-9a-f]{6}" s)))
(defn eye-color? [s] (#{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"} s))
(defn passport-id? [s] (re-matches #"[\d]{9}" s))

(def validation-checks {"byr" birth-year?
                        "iyr" issue-year?
                        "eyr" expiration-year?
                        "hgt" height?
                        "hcl" hair-color?
                        "ecl" eye-color?
                        "pid" passport-id?})

(defn passes-check? [[k v]]
  (if-let [f (validation-checks k)]
    (f v)
    true))

(defn valid-passport2 [passport]
  (every? passes-check? passport))

(defn part1 [input]
  (->> (parse-input input)
       (filter valid-passport?)
       count))

(defn part2 [input]
  (->> (parse-input input)
       (filter #(and (valid-passport? %) (valid-passport2 %)))
       count))