# Day Ten: Adapter Array

* [Problem statement](https://adventofcode.com/2020/day/10)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day10.clj)

---

Today's problem involved more simple manipulations of numbers. Nice and easy.

## Part 1

Our input is an unordered list of unique numbers, and the task is to count the differences between
each value and its next value. Then we need to multiply the number of times that distance was 1,
versus the number of times that distance was 3. The problem starts to start from value zero, and to 
assume that there is one more number 3 above the largest number in the list.

Step one, of course, is to parse the input. All we need is to split the lines, parse each as an Int,
and then stick a `0` in the front to help with the first comparison.

```clojure
(defn parse-jolts [input]
  (->> (str/split-lines input)
       (map #(Integer/parseInt %))
       (cons 0)))
```

Then we need to sort the jolts and compute the difference between each pair. I had planned to create
a sequence of pairs using `(partition 2 1 jolts)`, but I stumbled upon a detail about the `map`
function I hadn't known before. I've always used the form `(map f coll)` to apply function `f` to
each value in the sequence `coll`, but there are other forms. `(map f coll1 coll2)` applies function
`f` to the first value in `coll1` and `coll2`, then to the second value in `coll1` and `coll2`, etc. 
until one or more of the collections run out.  So I used `(map - (rest jolts) jolts)` to subtract
each value from its previous value in the collection; thus if `jolts=[1 3 6]`, then the function
would be `(map - [3 6] [1 3 6])` and would return `(2 3)`.

Given that sequence of distances, I needed to count the number of them that were 1 or were 3. It so
happens that there weren't any other distances in my data set, but I didn't want to depend on that.
So `(frequencies coll)` returns a map of `{v count}`, mapping each value to the number of times it
appears. I destructure that into the ones and threes with `({ones 1, threes 3} map)`, and then
we're almost done. Remember that the instructions say there's one more phantom value 3 greater than
the last value, so I have to increment the `threes` to derive the solution.

```clojure
(defn part1 [input]
  (let [jolts (-> input parse-jolts sort)
        {ones 1 threes 3} (->> (map - (rest jolts) jolts)
                               frequencies)]
    (* ones (inc threes))))
```   

---

## Part 2

For part 2, we need to know how many paths exist from 0 to the largest jolt, such that each transition
is a step between 1 and 3. Many of these kinds of solutions are best solved by working in reverse,
because that often turns depth-first and breadth-first searches into linear calculations, and this 
time was no different.

To start, we reverse the list of jolts. One approach would be to call `(-> jolts sort reverse)`, but
the `sort-by` function can do that in one simple step. By calling `(sort-by - jolts)`, we use the
`-` function on each element, thus comparing each element by its negative value.

Then I used a `reduce` function to calculate the data. The goal is to start from the largest number
(first in the reversed list), and assert that there is one path there. Then we backtrack through 
each element in the list, looking for all path counts from `n+1` through `n+3`, as those are the 
only other jolts we can reach. Add all of those mappings together, and `assoc` them into the map
of path values. When we're all done, the `part2` function takes the mapping counts, and retrieves
the value bound to `0`, since the actual problem looks for all paths from `0` to the last number.

```clojure
(defn paths-from [jolts]
  (let [rev (sort-by - jolts)]
    (reduce (fn [acc n]
              (->> (map + [1 2 3] (repeat n))
                   (keep #(acc %))
                   (apply +)
                   (assoc acc n)))
            {(first rev) 1}
            (rest rev))))

(defn part2 [input]
  (-> (parse-jolts input) paths-from (get 0)))
```