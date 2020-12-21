(ns advent-2020-clojure.day20
  (:require [clojure.string :as str]
            [advent-2020-clojure.cube-solver :as cube]
            [advent-2020-clojure.utils :as utils]))

(defn parse-tile [tile-str]
  (let [lines (str/split-lines tile-str)
        tile-num (->> (first lines)
                      (re-matches #"Tile (\d+):")
                      second
                      Integer/parseInt)]
    [tile-num (vec (rest lines))]))

(defn parse-input [input]
  (->> (utils/split-blank-line input)
       (map parse-tile)))

(defn grid-point [grid [x y]] (get-in grid [y x]))
(defn flip [grid] (mapv str/reverse grid))
(defn rotate [grid]
  (let [max-side (dec (count grid))]
    (->> (map-indexed (fn [y row]
                        (->> (map-indexed (fn [x _]
                                            (grid-point grid [y (- max-side x)]))
                                          row)
                             (apply str)))
                      grid)
         vec)))

(defn permutations [grid]
  (->> (list grid (flip grid))
       (map #(take 4 (iterate rotate %)))
       (apply concat)
       set))

(defn tile-permutations [tile]
  (->> (second tile)
       permutations
       (map #(vector (first tile) %))
       vec))

(defn left-of? [[_ grid-a] [_ grid-b]]
  (= (map last grid-a)
     (map first grid-b)))

(defn above? [[_ grid-a] [_ grid-b]]
  (= (last grid-a) (first grid-b)))

(defn corner-indexes [coll]
  (let [size (count coll)
        length (-> size Math/sqrt int)]
    (list 0 (dec length) (- size length) (dec size))))

(defn part1 [input]
  (let [tiles (parse-input input)
        board (cube/solve-cube above? left-of? tile-permutations tiles)]
    (->> (map #(nth board %) (corner-indexes board))
         (map first)
         (apply *))))