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

(defn part1 [input]
  (->> (parse-input input)
       (filter valid-passport?)
       count))