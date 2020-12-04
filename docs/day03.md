# Day Three: Toboggan Trajectory

* [Problem statement](https://adventofcode.com/2020/day/3)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day03.clj)

---

## Part 1

It's sledding time! Tobogganing time? I'm no longer a Yankee, so I can't tell the
difference anymore.

In this problem, we're given an infinite horizontal map of open spaces and trees,
and a path to go down a hill. The input is a map of open spaces (periods) and
trees (hash signs), and a direction of moving right three spaces and down one,
until we get below the input map.  As stated above, the map repeats horizontally.

Usually, I start with a `parse-input` function, but I skipped that today. We'll
just treat the input as a simple list of Strings using `(str/split-lines)`.
However, to keep our program context-relevant, I made a simple function `tree?`
to check if a given character is a tree. I also made a constant `origin` for the
starting point. Neither of these are critical, of course, but let's be clean.

Note that `origin` is a simple value, so we use `def`, while `tree?` is a function,
so we use `defn` as the combination of `def` and `fn`.

```clojure
(def origin [0 0])
(defn tree? [c] (= c \#))
``` 

Next, let's figure out every spot in the path that we will toboggan over. For this,
we'll use the `iterate` function to generate an infinite sequence, and then figure out
how to get out of it. `iterate` has a starting point, which will be the `origin`.
The next spot we _might_ hit requires taking the last starting point and adding the path
vector.  The easiest way to add together two vectors of integers is to call
`(map + v1 v2)`, which in this case would be `(map + [0 0] [3 1])`. Since the `iterate`
function feeds in its last value as the last parameter into its function, the `%` sign
in `#(map + path %)` is the previous calculation.

Then we just have to decide how many values to take out of the infinite sequence.
`take-while` takes in a predicate and a sequence, and returns all values from the 
sequence until the first value does not return `true` from the predicate. We only want
the `[x y]` vectors where `y` fits within the number of tree lines. I could use defined
the predicate as `(fn [[_ y]] (< y num-tree-lines))` but since there is only one
parameter, we can use the `#()` short-hand and call `(second %)` to take the second
value out of the input parameter sequence.  

```clojure
(defn target-coordinates [path num-tree-lines]
  (take-while
    #(< (second %) num-tree-lines)
    (iterate #(map + path %) origin)))
```

Now that we have a list of coordinates to hit, let's find out what's at each
coordinate. To handle the horizontal scrolling, I could have applied the `mod` 
function to the `x` value for each point; if the width of the row is 10 
characters and `x=13`, we could just look at the element at `x=3`. But that's no
fun. Instead, the `value-at` function takes the parsed map of tree lines and 
the target coordinate, then it finds the correct row, and then we apply the
`cycle` function. `cycle` takes a sequence and returns an infinite (lazy)
sequence that repeats the input sequence; `(cycle [1 2 3])` returns a lazy
sequence of `(1 2 3 1 2 3 1 2...)`. So now that the row continues forever, we
can apply `nth` to grab the cell we want.

Of note is the use of the thread-first macro `->`, instead of the thread-last
macro `->>` I've used extensively. This runs each expression in order, feeding
the result of each expression as the _first_ parameter of the next expression,
rather than sending it as the _last_ parameter like thread-last. In general,
operations on collections (`map`, `reduce`, `filter`) tend to use `->>` because
the collection argument is last, while operations on single values tend to use
`->`.

```clojure
(defn value-at [tree-lines row col]
  (-> (tree-lines row)
      cycle
      (nth col)))
```

Now the `solve` function is pretty simple. We split the input into a list of
lines, then find all of the target coordinates within the map. We then map each
coordinate to its value on the map, filter out only the values that are trees,
and count up the result.

```clojure
(defn solve [input path]
  (let [tree-lines (str/split-lines input)]
    (->> (target-coordinates path (count tree-lines))
         (map (fn [[x y]] (value-at tree-lines y x)))
         (filter tree?)
         count)))
```

And the final `part1` function just uses the `solve` function with the given
input and the path of `[x=3 y=1]`.

```clojure
(def part1-path [3 1])
(defn part1 [input]
  (solve input part1-path))
```

---

## Part 2

Part two uses the same basic logic as part 1, except that we now want to run
through the map multiple times, using several paths. We look at the number of
trees in each path, and then multiply the results together. Given the
construction of the `solve` function accepting the path to run, this is simple.

All we need to do is take each path (list of `[x y]` coordinates), map each to
the number of trees via the `solve` function, and use `(apply *)` to multiply
the results. Note again that we use thread-last (`->>`) since both `map` and
`apply` operate on collections.

```clojure
(def part2-paths [[1 1] [3 1] [5 1] [7 1] [1 2]])

(defn part2 [input]
  (->> part2-paths
       (map (partial solve input))
       (apply *)))
```

Happy tobogganing!
