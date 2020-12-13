(ns advent-2020-clojure.day12
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]))

(defn new-ship [initial-waypoint]
  {:ship [0 0] :waypoint initial-waypoint})

(def dir-amounts {:north [0 -1]
                  :south [0 1]
                  :east  [1 0]
                  :west  [-1 0]})

(defn move [state mover target amt]
  (let [move-by (mapv * target [amt amt])]
    (update state mover (partial mapv + move-by))))

(defn slide [state mover dir amt]
  (move state mover (dir-amounts dir) amt))

(defn follow-waypoint [state amt]
  (move state :ship (state :waypoint) amt))

(defn rotate-waypoint [state degrees]
  (let [times (-> degrees (mod 360) (quot 90))
        rotate90 (fn [[x y]] [(- y) x])]
    (-> (iterate
          (fn [s] (update s :waypoint rotate90))
          state)
        (nth times))))

(defn next-state [state mover line]
  (let [op (first line)
        amt (-> (subs line 1) Integer/parseInt)]
    (case op
      \N (slide state mover :north amt)
      \S (slide state mover :south amt)
      \E (slide state mover :east amt)
      \W (slide state mover :west amt)
      \L (rotate-waypoint state (- amt))
      \R (rotate-waypoint state amt)
      \F (follow-waypoint state amt))))

(defn solve [initial-waypoint mover input]
  (->> (reduce #(next-state %1 mover %2)
               (new-ship initial-waypoint)
               (str/split-lines input))
       :ship
       (mapv utils/abs)
       (apply +)))

(defn part1 [input]
  (solve [1 0] :ship input))

(defn part2 [input]
  (solve [10 -1] :waypoint input))
