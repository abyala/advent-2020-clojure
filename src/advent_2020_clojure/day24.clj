(ns advent-2020-clojure.day24
  (:require [clojure.string :as str]))

(def directions {:e [2 0] :ne [1 1] :se [1 -1] :w [-2 0] :nw [-1 1] :sw [-1 -1]})

(defn parse-path [input]
  (->> input (re-seq #"[n|s]?[e|w]") (map keyword)))

(defn tile-at [path]
  (reduce #(mapv + %1 %2)
          [0 0]
          (map #(directions %) path)))

(defn initial-black-tiles [input]
  (->> (str/split-lines input)
       (map (partial (comp tile-at parse-path)))
       frequencies
       (keep (fn [[tile freq]] (when (odd? freq) tile)))
       (into #{})))

(defn part1 [input]
  (-> input initial-black-tiles count))

(defn adjacencies [tile]
  (->> (vals directions)
       (map #(mapv + tile %))))

(defn next-side-black? [tile black-tiles]
  (let [black-adjacent (->> (adjacencies tile)
                            (filter #(black-tiles %))
                            count)]
    (if (black-tiles tile)
      (#{1 2} black-adjacent)
      (= 2 black-adjacent))))

(defn next-turn [tile-set]
  (->> tile-set
       (mapcat adjacencies)
       (into tile-set)
       (filter #(next-side-black? % tile-set))
       set))

(defn part2 [input]
  (->> (iterate next-turn (initial-black-tiles input))
       (drop 100)
       (map count)
       first))