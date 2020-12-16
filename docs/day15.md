# Day Fifteen: Rambunctious Recitation

* [Problem statement](https://adventofcode.com/2020/day/15)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day15.clj)

---

Today's program was rather simple, to the point that I don't really feel like cleaning it up or finding clever
tricks to share. I usually try to solve the problem as though I'd be supporting the features in a real project,
but today I'm happy to just make one big honking function.

## Parts 1 and 2

I won't repeat the problem statement, but we need to read a set of initial data values, run a calculation on
the last value "spoken" as a function of the most recent time that spoken value was seen, if at all. The whole
function is just one big loop that terminates when `turns-finished` hits 2020.  If anything, the complexity
surrounded avoiding off-by-one issues, but again, I didn't find that particularly interesting.

So I'll just mention one function I've seen other AoC participants use which I hadn't yet - `butlast`. Just as
we use `next` and `rest` on a collection to skip the first value and use the rest, `butlast` takes a collection
and returns all elements except for the last one. For normal lists/sequences, it's not an efficient function to
use, since list operations should tend to use the front of the list, while vectors should tend to use the back.
But in this case, it seemed valid.

So yeah. The solution works quickly for `part1`, where the target is 2020, and it's acceptable for `part2, where
the target is 30000000. Sorry for such a short write-up!

```clojure
(defn solve [input target]
  (let [nums (->> (str/split input #",")
                  (map-indexed (fn [idx v] [(Integer/parseInt v) (inc idx)])))
        initial-map            (into {} (butlast nums))
        initial-spoken         (-> nums last first)
        initial-turns-finished (count nums)]
    (loop [data           initial-map,
           last-spoken    initial-spoken,
           turns-finished initial-turns-finished]
      (if (= target turns-finished)
        last-spoken
        (recur (assoc data last-spoken turns-finished)
               (if-let [last-spoken-idx (data last-spoken)]
                 (- turns-finished last-spoken-idx)
                 0)
               (inc turns-finished))))))

(defn part1 [input] (solve input 2020))
(defn part2 [input] (solve input 30000000))
```