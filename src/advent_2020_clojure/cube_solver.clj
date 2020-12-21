(ns advent-2020-clojure.cube-solver)

(defn new-possible-square [size] {:data   []
                                  :size   size
                                  :length (-> size Math/sqrt int)})

(defn next-index [possible-square] (count (:data possible-square)))
(defn append-to-square [possible-square v]
  (update possible-square :data #(conj % v)))
(defn solved? [{:keys [data size]}]
  (= size (count data)))

(defn left-of-gap [{:keys [data length] :as possible-square}]
  (let [index (next-index possible-square)]
    (when (pos? (mod index length))
      (last data))))

(defn above-gap [{:keys [data length] :as possible-square}]
  (let [index (next-index possible-square)]
    (when (>= index length)
      (data (- index length)))))

(defn fits-next? [possible-square left-of-pred above-pred v]
  (and (if-let [left (left-of-gap possible-square)]
         (left-of-pred left v)
         true)
       (if-let [above (above-gap possible-square)]
         (above-pred above v)
         true)))

(defn next-possibilities [possible-square groups left-of-pred above-pred]
  (for [group groups
        v group
        :let [others (remove #(= % group) groups)]
        :when (fits-next? possible-square left-of-pred above-pred v)]
    [(append-to-square possible-square v) others]))

(defn solve-cube [left-of-pred
                  above-pred
                  permutation-fn
                  data]
  (loop [possibilities (next-possibilities (new-possible-square (count data))
                                           (map permutation-fn data)
                                           left-of-pred
                                           above-pred)]
    (let [[possible-square unused] (first possibilities)]
      (if (solved? possible-square)
        (possible-square :data)
        (recur (into (rest possibilities)
                     (next-possibilities possible-square unused left-of-pred above-pred)))))))