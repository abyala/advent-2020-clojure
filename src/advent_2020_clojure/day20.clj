(ns advent-2020-clojure.day20
  (:require [clojure.string :as str]
            [advent-2020-clojure.cube-solver :as cube]
            [advent-2020-clojure.utils :as utils]))

(defn wave? [c] (= c \#))

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

(defn solve [input]
  (cube/solve-cube left-of? above? tile-permutations (parse-input input)))

(defn part1 [input]
  (let [board (solve input)]
    (->> (map #(nth board %) (corner-indexes board))
         (map first)
         (apply *))))

(defn strip-border [grid]
  (->> (butlast (rest grid))
       (map #(subs % 1 (dec (count %))))))

(defn row-of-grids [grids]
  (->> grids
       (apply interleave)
       (partition (count grids))
       (map (partial apply str))))

(defn board-as-string [board]
  (let [size (count board)
        length (-> size Math/sqrt int)]
    (->> board
         (map strip-border)
         (partition length)
         (map row-of-grids)
         (apply concat)
         vec)))

(def monster
  (->> (str/split-lines "                  # \n#    ##    ##    ###\n #  #  #  #  #  #   ")
       (keep-indexed (fn [y row]
                       (keep-indexed (fn [x c]
                                       (when (= \# c) [x y]) ) row)))
       (apply concat)))

(defn monster? [board x y]
  (every? (fn [[mx my]]
            (= \# (-> board (nth (+ y my) nil) (nth (+ x mx) nil))))
          monster))

(defn replace-char-at [word idx c]
  (apply str (subs word 0 idx) (str c) (subs word (inc idx))))

(defn murder-monster [board x y]
  (reduce (fn [b [mx my]] (update b (+ y my) #(replace-char-at % (+ x mx) \space)) )
          board
          monster))

(defn remove-monsters
  ([the-board] (remove-monsters the-board 0 0))
  ([the-board pos-x pos-y]
   (loop [board the-board x pos-x y pos-y found? false]
     (cond
       (>= y (count board))         (when found? board)
       (>= x (count (first board))) (recur board 0 (inc y) found?)
       (monster? board x y)         (recur (murder-monster board x y) (inc x) y true)
       :else                        (recur board (inc x) y found?)))))

(defn part2 [input]
  (->> input
       solve
       (map second)
       board-as-string
       permutations
       (keep (partial remove-monsters))
       (map #(->> (apply concat %) (filter wave?) count))
       first))