(ns advent-2020-clojure.day08
  (:require [clojure.string :as str]))

; State: {:offset n
;         :acc x
;         :instructions [[cmd args]]}

(defn parse-console [input]
  {:offset       0
   :acc          0
   :instructions (->> (str/split-lines input)
                      (mapv #(str/split % #" ")))})

(defn parse-num [s]
  (->> (subs s 1)
       (Integer/parseInt)
       (* (if (= \+ (first s)) 1 -1))))

(defn move-offset
  ([state] (move-offset state 1))
  ([state n] (update state :offset + n)))

(defn op-nop [state & _] (move-offset state))
(defn op-acc [state arg]
  (-> state
      (update :acc + (parse-num arg))
      move-offset))
(defn op-jmp [state arg]
  (move-offset state (parse-num arg)))

(defn run-next-op [state]
  (when (< (state :offset) (count (state :instructions)))
    (let [[ins arg] (-> state :instructions (nth (state :offset)))]
      (case ins
        "nop" (op-nop state arg)
        "acc" (op-acc state arg)
        "jmp" (op-jmp state arg)))))

(defn run-to-completion [state]
  (first (->> (iterate (fn [[s seen _]]
                         [(run-next-op s) (conj seen (:offset s)) s])
                       [state #{} nil])
              (map (fn [[s seen prev]]
                     (cond
                       (nil? s) {:status :terminated, :state prev}
                       (contains? seen (:offset s)) {:status :loop, :state s}
                       :else {:status :running, :state s})))
              (filter #(not= :running (:status %))))))

(defn part1 [input]
  (let [state (parse-console input)]
    (->> (iterate (fn [[s seen]]
                    [(run-next-op s) (conj seen (:offset s))])
                  [state #{}])
         (filter (fn [[s seen]] (contains? seen (:offset s))))
         ffirst
         :acc)))

(defn part2 [input]
  (let [state (parse-console input)
        possible-states (->> (:instructions state)
                             (keep-indexed (fn [idx [op]]
                                             (when-let [next-op ({"jmp" "nop" "nop" "jmp"} op)]
                                               (assoc-in state [:instructions idx 0] next-op)))))]
    (->> possible-states
         (map run-to-completion)
         (keep (fn [{:keys [status state]}]
                 (when (= :terminated status) (:acc state))))
         first)))