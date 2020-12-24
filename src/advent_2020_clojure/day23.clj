(ns advent-2020-clojure.day23)

(defn init-cups [input]
  (let [cup-ids (map #(Integer/parseInt (str %)) input)
        data-map (->> (partition 2 1 cup-ids cup-ids)
                      (map vec)
                      (into {}))]
    {:current (first cup-ids) :cups data-map :size (count cup-ids)}))

(defn next-cup [state id]
  (get-in state [:cups id]))

(defn cup-seq
  ([state] (cup-seq state (:current state)))
  ([state lookup]
   (let [v (next-cup state lookup)]
     (lazy-seq (cons v (cup-seq state v))))))

(defn destination-cup [{:keys [current size]} disallowed]
  (loop [v (dec current)]
    (cond
      (zero? v) (recur size)
      (disallowed v) (recur (dec v))
      :else v)))

(defn next-turn [{:keys [current cups] :as state}]
  (let [[pick1 pick2 pick3 next-current] (cup-seq state)
        dest (destination-cup state #{pick1 pick2 pick3})
        next-dest (next-cup state dest)]
    (assoc state :current next-current
                 :cups (assoc cups current next-current
                                   dest pick1
                                   pick3 next-dest))))

(defn play-game [state turns]
  (-> (iterate next-turn state)
      (nth turns)))

(defn label [state]
  (->> (cup-seq state 1)
       (take-while #(not= % 1))
       (apply str)))

(defn part1 [input num-turns]
  (-> (init-cups input)
      (play-game num-turns)
      label))

(defn tail [{:keys [current cups]}]
  (->> cups
       (keep (fn [[k v]]
               (when (= current v) k)))
       first))

(defn extend-cups [state max-cup]
  (let [{:keys [current cups]} state
        extension-start (inc (apply max (keys cups)))
        extension-end (inc max-cup)
        extensions (->> (partition 2 1 [current] (range extension-start extension-end))
                        (map vec)
                        (into {(tail state) extension-start}))]
    (assoc state :cups (merge cups extensions)
                 :size max-cup)))

(defn part2 [input max-cup num-turns]
  (let [state (-> (init-cups input) (extend-cups max-cup))
        answer (play-game state num-turns)]
    (->> (cup-seq answer 1)
         (take 2)
         (apply *))))