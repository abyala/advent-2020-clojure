(ns advent-2020-clojure.day08
  (:require [clojure.string :as str]
            [advent-2020-clojure.game-console :as game]))

(defn parse-input [input]
  (game/init-console (->> (str/split-lines input)
                          (mapv #(str/split % #" ")))))

(defn run-to-completion [state]
  (loop [s state seen #{}]
    (let [{:keys [offset] :as next-state} (game/run-next-op s)]
      (cond
        (nil? next-state) {:status :terminated, :state s}
        (seen offset) {:status :loop, :state next-state}
        :else (recur next-state (conj seen offset))))))

(defn part1 [input]
  (-> (parse-input input)
       run-to-completion
       (get-in [:state :acc])))

(defn alternate-instructions [instructions]
  (keep-indexed (fn [idx [op]]
                  (when-let [next-op ({"jmp" "nop" "nop" "jmp"} op)]
                    (assoc-in instructions [idx 0] next-op)))
                instructions))

(defn part2 [input]
  (let [{:keys [instructions] :as state} (parse-input input)]
    (->> (alternate-instructions instructions)
         (map #(run-to-completion (assoc state :instructions %)))
         (keep (fn [{:keys [status state]}]
                 (when (= :terminated status) (:acc state))))
         first)))