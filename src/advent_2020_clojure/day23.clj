(ns advent-2020-clojure.day23
  (:require [clojure.string :as str]))

(defn init-cups [input]
  (let [cup-ids (mapv #(Integer/parseInt (str %)) input)
        data-map (->> (interleave cup-ids (-> cup-ids rest vec
                                              (conj (first cup-ids))))
                      (partition 2)
                      (map vec)
                      (into {}))]
    {:current (first cup-ids) :cups data-map :size (count data-map)}))

(defn next-cup [state id]
  (get-in state [:cups id]))

(defn destination-cup [current pick1 pick2 pick3 size]
  (->> (concat (range (dec current) 0 -1)
               (range size 0 -1))
       (filter #(not (#{pick1 pick2 pick3 current} %)))
       first))

(defn next-turn-2 [{:keys [current cups size] :as state}]
  (let [[_ pick1 pick2 pick3 next-current] (take 5 (iterate #(next-cup state %) current))
        dest (destination-cup current pick1 pick2 pick3 size)
        next-dest (next-cup state dest)]
    (assoc state :current next-current
                 :cups (assoc cups current next-current
                                   dest pick1
                                   pick3 next-dest))))

; *********
; **** LATER: Make a sequence of the next values in order
; *********

(defn label [state]
  (loop [acc "" idx 1]
    (if (= (count acc) (dec (:size state)))
      acc
      (let [next-idx (next-cup state idx)]
        (recur (str acc next-idx) next-idx)))))

(defn part1 [input num-turns]
  (let [state (init-cups input)]
    (->> (iterate next-turn-2 state)
         (drop num-turns)
         first
         label)))

(defn extend-cups [state max-cup]
  (let [{:keys [current cups]} state
        tail (->> cups
                  (keep #(when (= current (second %)) (first %)))
                  first)
        extension-start (inc (apply max (keys cups)))
        extension-end (inc max-cup)
        extensions (assoc (->> (interleave (range extension-start extension-end)
                                           (range (inc extension-start) extension-end))
                               (partition 2)
                               (map vec)
                               (into {}))
                     tail extension-start
                     max-cup current)]
    (assoc state :cups (merge cups extensions)
                 :size max-cup)))

(defn part2 [input max-cup num-turns]
  (let [state (-> (init-cups input)
                   (extend-cups max-cup))
        answer (-> (iterate next-turn-2 state)
                   (nth num-turns))]
    (->> (iterate #(next-cup answer %) 1)
         rest
         (take 2)
         (apply *))))