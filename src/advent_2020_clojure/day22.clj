(ns advent-2020-clojure.day22
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]))

(defn parse-player [s]
  (->> (str/split-lines s)
       rest
       (map #(Integer/parseInt %))))

(defn parse-board [input]
  (->> (utils/split-blank-line input)
       (map parse-player)))

(defn create-subgame? [board recursive?]
  (and recursive?
       (every? #(<= (first %) (count (rest %)))
               board)))

(defn subdecks [board]
  (map #(take (first %) (rest %)) board))

(defn play-game [starting-board recursive?]
  (loop [[deck1 deck2 :as board] starting-board, seen #{}]
    (cond
      (empty? deck1) {:winner :player2 :deck deck2}
      (empty? deck2) {:winner :player1 :deck deck1}
      (seen board) {:winner :player1}   ; Assume this doesn't happen at the top-level!
      :else (let [[a b] (map first board)
                  winner (cond
                           (create-subgame? board recursive?) (-> (subdecks board)
                                                                  (play-game recursive?)
                                                                  :winner)
                           (> a b) :player1
                           :else :player2)]
              (recur (case winner
                       :player1 [(concat (rest deck1) (list a b)) (rest deck2)]
                       :player2 [(rest deck1) (concat (rest deck2) (list b a))])
                     (conj seen board))))))

(defn score [deck]
  (->> (range (count deck) 0 -1)
       (map * deck)
       (apply +)))

(defn solve [input recursive?]
  (let [board (parse-board input)]
    (->> (play-game board recursive?)
         :deck
         score)))

(defn part1 [input] (solve input false))
(defn part2 [input] (solve input true))