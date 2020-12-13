(ns advent-2020-clojure.day13
  (:require [clojure.string :as str]))


(defn part1 [input]
  (let [lines (str/split-lines input)
        earliest (-> (first lines) Integer/parseInt)
        busses (->> (str/split (second lines) #",")
                    (keep #(when (not= % "x") (Integer/parseInt %))))]
    (let [options (for [b busses]
                    (vector b (->> (iterate (partial + b) b)
                                   (drop-while (partial > earliest))
                                   first)))]
      (->> options
           (sort-by second)
           (map (fn [[id start]] (* id (- start earliest))))
           first))))

(defn part2-cheater [input]
  (let [lines (str/split-lines input)
        demands (->> (str/split (second lines) #",")
                     (keep-indexed (fn [idx v] (when (not= v "x") [idx (Integer/parseInt v)])))
                     (into {}))]
    (->> (map (fn [[idx v]] (str "(t + " idx ") mod " v " = 0")) demands)
         (str/join ", "))))

(defn part2-thank-you-todd [input]
  (let [lines (str/split-lines input)
        busses (->> (str/split (second lines) #",")
                    (keep-indexed (fn [idx v] (when (not= v "x")
                                                [idx (Integer/parseInt v)]))))]
    (loop [step-size (-> busses first second)
           time 0
           [[idx bus] & other-busses] (rest busses)]
      (if (nil? bus)
        time
        (recur (* step-size bus)
               (->> (iterate (partial + step-size) time)
                    (filter #(zero? (-> (+ idx %) (mod bus))))
                    first)
               other-busses))))
  )