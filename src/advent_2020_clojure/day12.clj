(ns advent-2020-clojure.day12
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]))

; State: [[x y] dir]
(def dirs [:north :east :south :west])
(defn rotate [dir amt]
  (-> (.indexOf dirs dir)
      (+ (quot amt 90))
      (mod (count dirs))
      dirs))

(def dir-amounts {:north [0 -1]
                  :south [0 1]
                  :east  [1 0]
                  :west  [-1 0]})

(defn move [[[x y] dir] amt]
  (->> (dir-amounts dir)
       (mapv * [amt amt])
       (mapv + [x y])))

(defn next-state [[[x y] dir :as state] line]
  (let [op (first line)
        amt (-> (subs line 1) Integer/parseInt)]
    (case op
      \N [[x (- y amt)] dir]
      \S [[x (+ y amt)] dir]
      \E [[(+ x amt) y] dir]
      \W [[(- x amt) y] dir]
      \L [[x y] (rotate dir (- amt))]
      \R [[x y] (rotate dir amt)]
      \F [(move state amt) dir])))

(defn part1 [input]
  (->> (reduce #(next-state %1 %2)
               [[0 0] :east]
               (str/split-lines input))
       first
       (mapv utils/abs)
       (apply +)))



; New state: [[x y] [wx wy]

(defn rotate-2 [point amt]
  (loop [[x y] point n (-> amt (mod 360) (quot 90))]
    (if (zero? n)
      [x y]
      (recur [(- y) x] (dec n))))

  #_(case (mod amt 360)
      0 [x y]
      90 [(- y) (- x)]
      180 [(- x) (- y)]
      270 [y x]))

(defn move2 [[ship waypoint] amt]
  (->> (map * waypoint [amt amt])
       (map + ship)))

(defn next-state2 [[[x y] [wx wy] :as state] line]
  (let [op (first line)
        amt (-> (subs line 1) Integer/parseInt)]
    (case op
      \N [[x y] [wx (- wy amt)]]
      \S [[x y] [wx (+ wy amt)]]
      \E [[x y] [(+ wx amt) wy]]
      \W [[x y] [(- wx amt) wy]]
      \L [[x y] (rotate-2 [wx wy] (- amt))]
      \R [[x y] (rotate-2 [wx wy] amt)]
      \F [(move2 state amt) [wx wy]])))

(defn part2 [input]
  (->> (reduce #(next-state2 %1 %2)
               [[0 0] [10 -1]]
               (str/split-lines input))
       first
       (mapv utils/abs)
       (apply +)))
