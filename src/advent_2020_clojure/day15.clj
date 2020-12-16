(ns advent-2020-clojure.day15
  (:require [clojure.string :as str]))

(defn solve [input target]
  (let [nums (->> (str/split input #",")
                  (map-indexed (fn [idx v] [(Integer/parseInt v) (inc idx)])))
        initial-map            (into {} (butlast nums))
        initial-spoken         (-> nums last first)
        initial-turns-finished (count nums)]
    (loop [data           initial-map,
           last-spoken    initial-spoken,
           turns-finished initial-turns-finished]
      (if (= target turns-finished)
        last-spoken
        (recur (assoc data last-spoken turns-finished)
               (if-let [last-spoken-idx (data last-spoken)]
                 (- turns-finished last-spoken-idx)
                 0)
               (inc turns-finished))))))

(defn part1 [input] (solve input 2020))
(defn part2 [input] (solve input 30000000))