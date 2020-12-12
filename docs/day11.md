# Day Eleven: Seating System

* [Problem statement](https://adventofcode.com/2020/day/11)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day11.clj)

---

Well the problems are officially becoming more difficult.  Or perhaps my solutions are just becoming messier.
Either way, problem 11 is done and there's something to show for it. Because I like to solve parts 1 and 2
in very similar ways, I'm going to describe both problem statements together up-front, which will explain 
why some functions are a bit more parameterized (read: complex).

We are given a grid of positions within a floor, where each character is one of three characters: `.` for
a blank/unused space, `L` for an empty seat, or `#` for an occupied seat. The program requires looking at
every seat (empty or occupied) within the floor, deciding how many seats in each direction are considered
occupied, and then decide whether an occupied seat should be emptied, and/or an empty seat should be
occupied. There are two parameters that distinguish part 1 from part 2. First, the algorithm to apply to
decide which seat to inspect in each direction when deciding if it's occupied. And second, what I call the
`awkwardness,` which says how many neighboring occupied seats is enough to encourage someone in that seat
to empty it.

---

# Part 1

The lion's share of the work goes into part 1, given that we're going to parameterize the "looking function"
and the awkwardness.
 
First, I prepare a few simple definitions to make the logic more business-y.  Namely, I don't want to
pepper the code with the characters `.` and `L` and `#`, so I make constants `occupied-seat`, `empty-seat`,
and `space`, and predicates `occupied-seat?`, `empty-seat?` and `seat?` to get that out of the subsequent
logic.

```clojure
(def occupied-seat \#)
(def empty-seat \L)
(def space \.)
(defn occupied-seat? [c] (= occupied-seat c))
(defn empty-seat? [c] (= empty-seat c))
(defn seat? [c] (or (occupied-seat? c) (empty-seat? c)))
```

Now I've decided not to do any fancy parsing with the data this time. Normally I would consider reading
each line, maybe creating a map of each `[x y]` coordinate to its value, but I found it easier this time
to just split the lines and consider a `grid` to be a list of Strings. Because of this, my coordinates 
are expressed as `[y x]` instead of `[x y]`, since `(get-in grid [y x])` works naturally, by taking the
`nth` String within the `grid` as the `y` value, and then then `nth` character within the String as the
`x` value.  

So the next big task is to take a point within a grid, and return a list of all points seen when looking
in each of the 8 directions. First, I defined a var called `all-directions`, which shows the slopes of lines
to follow. Note that `[0 0]` is not in the list, since we need some movement. I thought about calculating
this, but sometimes a constant is just what we want.

```clojure
(def all-directions '([-1 -1] [-1 0] [-1 1]
                      [0 -1] [0 1]
                      [1 -1] [1 0] [1 1]))
```

To build out the `all-neighbor-paths`, we'll use a little `for` construct. First, I'll count the number
of rows and columns in the grid, by looking at `(count grid)` for the rows, and `(count (first grid))` for
the columns.  Note that these don't change within the program, so in theory I could have calculated them
once and made the grid into a more complex data structure, but I thought this is fine. Then, I defined
a helper inner function called `in-range?`, which ensures that any `[y x]` point is within the grid.

Finally, I loop over all of the directions, and set up a little threading. The key to this is the expression
`(iterate (partial map + dir) point)`, which returns a lazy sequence of points. Starting with the `point`
that's passed in to the function, and a direction `dir`, we call `(map + dir point)` to add the slope to
the point, thus "walking" in the chosen direction. Then we use `rest` to drop the first value, since it's
the originating point, and `(take-while in-range?)` to keep only the values that fall within the map.

So this should return a list of 8 element, each being a list of `[y x]` points.

```clojure
(defn all-neighbor-paths [grid point]
  (let [rows (count grid)
        cols (count (first grid))
        in-range? (fn [[y x]] (and (< -1 y rows)
                                   (< -1 x cols)))]
    (for [dir all-directions]
      (->> (iterate (partial map + dir) point)
           rest
           (take-while in-range?)))))
```

Now that we can look in all directions, we need a function `first-in-path` to return the first element
with a list of `[y x]` points whose value in the grid passes our "looking function," which we can still
abstract away for now. So all we do is start with the path (list of points), map each point into its
value on grid using `(get-in grid point)`, filter the values by the looking function `f`, and return
the first value if there is any.

```clojure
(defn first-in-path [grid f path]
  (->> path
       (map (partial get-in grid))
       (filter f)
       first))
```

Ok, let's recap where we are. Given a map, a starting point, and looking function, return the value of
the first point in the map that meets the looking function's needs. Now we need to see how many of these
values around the point are occupied, so we'll make `occupied-neighbors-by`. to start, we'll call
`all-neighbor-paths` to find the 3 to 8 neighboring paths. For each path, map it to `first-in-path` with
the looking function to get back a list of spaces in the grid. Then we select out the occupied ones using
`(filter occupied-seat? col)`, and `count` the results.

```clojure
(defn occupied-neighbors-by [grid point f]
  (->> (all-neighbor-paths grid point)
       (map (partial first-in-path grid f))
       (filter occupied-seat?)
       count))
```

So next we want to build out the function `next-turn`, which takes a grid and returns the next grid after
calculating each point. The fact that we need to do some logic on each point says we should build a
`next-point` function first. This function models the core business logic about how to change a seat.
I won't go line-by-line, but we figure out the current value in the grid as `c`, find out how many
neighbors are occupied using `occupied-neighbors-by`, and then use those values and the `awkwardness`
factor to either occupy the seat, empty it, or keep it the same. I threw in a little optimization near
the top, which says that if the point isn't even a seat (it's a space), then don't do any calculations
since spaces never change.

```clojure
(defn next-point [grid point f awkwardness]
  (let [c (get-in grid point)]
    (if-not (seat? c)
      space
      (let [occ (occupied-neighbors-by grid point f)]
        (cond
          (and (empty-seat? c) (zero? occ)) occupied-seat
          (and (occupied-seat? c) (>= occ awkwardness)) empty-seat
          :else c)))))
```


Ok, _now_ we can implement `next-turn`. I thought about using nested `for` macros, but opted
for nested `mapv-indexed` functions; `mapv-indexed` is a utility function I wrote to call
`map-indexed` and then `vec` to get back a vector.  I'm not sure why there's `map` and `mapv`,
and `map-indexed` but no `mapv-indexed`. Anyway, for each point we call `next-point` to
calculate its value, which essentially gives us the next grid.

```clojure
(defn next-turn [grid f awkwardness]
  (mapv-indexed (fn [y row]
                  (mapv-indexed (fn [x _]
                                  (next-point grid [y x] f awkwardness))
                                row))
                grid))
```

At least, the meat of the problem. We want to run `next-turn` until the grid from run `n` is
the same as the grid from run `n+1`. Again, `iterate` is the work horse, taking the grid
we get from just calling `(str/split-lines input)` and feeding it in to the `next-turn`
function for a lazy sequence. `(partition 2 1 col)` will return every pair of elements and
the next element, which we then filter for equality to see if the grid has stablized. If so,
we call `ffirst`, which is the same as `(first (first))` to get the first such pair of
identical values, and to then pull out the first grid. Then it's smooth sailing from here -
flatten and call `str` to get one big String, filter every character for only the occupied
ones, and count them up.

```clojure
(defn solve [input f awkwardness]
  (->> (iterate #(next-turn % f awkwardness)
                (str/split-lines input))
       (partition 2 1)
       (filter (partial apply =))
       ffirst
       (apply str)
       (filter occupied-seat?)
       count))
```

Finally, we implement our tiny `part1` function by calling `solve` with a looking function of
`some?` and an awkwardness of `4`. We use `some?` because when we have a list of points in
a path and we want the immediate neighbor, `some?` returns `true` if the value is not `nil`,
so `(first (map some? col))` is the same as `(first col)`.

```clojure
(defn part1 [input] (solve input some? 4))
```

--- 

## Part 2

Whew, that was a lot of work.  Was it worth it?

```clojure
(defn part2 [input] (solve input seat? 5))
```

It sure was!  We use a looking function of `seat?` because when looking in a path, we'll
accept any value that isn't an open space. Other than the awkwardness factor being `5`,
the rest of the algorithm holds.  We're done!
 