(ns advent-2020-clojure.day10
  (:require [clojure.string :as str]))

(defn parse-jolts [input]
  (->> (str/split-lines input)
       (map #(Integer/parseInt %))
       (cons 0)))

(defn part1 [input]
  (let [jolts (-> input parse-jolts sort)
        {ones 1 threes 3} (->> (map - (rest jolts) jolts)
                               frequencies)]
    (* ones (inc threes))))

(defn paths-from [jolts]
  (let [rev (sort-by - jolts)]
    (reduce (fn [acc n]
              (->> (map + [1 2 3] (repeat n))
                   (keep #(acc %))
                   (apply +)
                   (assoc acc n)))
            {(first rev) 1}
            (rest rev))))

(defn part2 [input]
  (-> (parse-jolts input) paths-from (get 0)))