(ns advent-2020-clojure.game-console)

; State: {:offset n
;         :acc x
;         :instructions [[cmd args]]}

(defn init-console [instructions]
  {:offset       0
   :acc          0
   :instructions instructions})

(defn move-offset
  ([state] (move-offset state 1))
  ([state n] (update state :offset + n)))

(defn op-nop [state & _] (move-offset state))
(defn op-acc [state arg]
  (-> state
      (update :acc + (Integer/parseInt arg))
      move-offset))
(defn op-jmp [state arg]
  (move-offset state (Integer/parseInt arg)))

(defn run-next-op [{:keys [instructions offset] :as state}]
  (when-let [[ins arg] (nth instructions offset nil)]
    (let [op ({"nop" op-nop
               "acc" op-acc
               "jmp" op-jmp} ins)]
      (op state arg))))