# Day Twenty: Jurassic Jigsaw

* [Problem statement](https://adventofcode.com/2020/day/20)
* Solution code
** [Day 20 puzzle](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day20.clj)
** [Cube Solver](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/cube-solver.clj)

---

I'll admit - today's problem was difficult, mostly because the data structure was quite complex.  This may sound
strange, but I found the easiest way to solve this problem was to first create a generic solution, and then plug
this problem into it. It was definitely overkill, but it helped me sort things out.

The problem states that we have a number of numbered tiles that we want to place into a square grid. Each tile 
may be oriented any which way, meaning we can rotate it and/or flip it, but all adjacent tiles must have matching
rows or columns. There are quite a lot of permutations to consider, never mind the complexity of matching adjacent 
cells or keeping track of which cell was which.

## Mental model

When I was a kid, I had a little travel game, which was a set of 2x2" cardboard tiles. On each edge of each tile,
there was either the front or back half of a turtle, with varying colors and shapes on their back. The challenge
was to form a square of them, such that the outsides could look messy, but every turtle had a front half, a back
half, and matching colors and shapes. Needless to say, this is exactly the same problem as what we have here, but
I found it easier to abstract away the details of the matches, thinking instead of the simpler turtle problem.
I played around with a few implementations of this model in the REPL before applying it to my program.

Since "naming things" is one of the two hardest programming tasks, beside cache invalidation and off-by-one errors,
I opted to call my construct "Cube Solver." It has nothing to do with turtles or "Jurrasic Jigsaws," and in truth
this models a large square and not a cube, but it made sense to me so I stuck with it.

## Cube Solver

To understand the Cube Solver, let's first look at the signature of the `solve-cube` function, without looking at
its implementation.

```clojure
(defn solve-cube [left-of-pred
                  above-pred
                  permutation-fn
                  data]
    ; Implementation TBD)
```

To solve a so-called cube, I need to pass in four pieces of data. First is a predicate that will answer if tile
`A` can sit to the left of tile `B`. Second is a similar predicate that will answer if tile `A` can sit above
tile `B`. Third is a function that takes in a tile and returns all possible orientations of that tile. Fourth
is the data itself, represented as a sequence of tiles.

In the turtle example, `left-of-pred` would look at `(:right A)` and `(:right B)` and see if the composed turtle
is a plausible animal. Similarly, `above-pred` would look at `(:bottom A)` and `(:top B)`. `permutation-fn` would
return all 4 orientations of the tile. And data would be the tiles. Note that this `solve-cube` function has no
concept of the data being applied to it.

To implement it, let's start with the primary data construct, a `possible-square`. (Yep, my Cube Solver works with
squares. Still no justification for this.)  A `possible-square` is essentially a partial solution to the problem,
meaning an incomplete square/cube. It's defined as a map of the current vector of data element, the target size of
tiles, and the length, assuming that we're dealing with a cube and not a rectangle.  `new-possible-square` constructs
the empty solution.

```clojure
(defn new-possible-square [size] {:data   []
                                  :size   size
                                  :length (-> size Math/sqrt int)})
```

Next we have three helper functions that work on a `possible-square`. `next-index` returns the index of the first
available value in the `:data` of the `possible-square`, which is essentially the count of the data.
`append-to-square` sticks a value onto the end of the `:data` vector of a `possible-square` using `conj`. And
`solved?` checks if the `possible-square` has successfully filled up, meaning its size matches the number of data
elements.

```clojure
(defn next-index [possible-square]         (count (:data possible-square)))
(defn append-to-square [possible-square v] (update possible-square :data #(conj % v)))
(defn solved? [{:keys [data size]}]        (= size (count data)))
```

Then we have two similar-looking functions called `left-of-gap` and `above-gap`. `left-of-gap` checks if the 
location of the next value in the `possible-square` has a neighbor to the left, and if so, returns that neighbor's
value. `above-gap` does the same, but looking up.  Those two functions work together with `fits-next?`, which takes
in a `possible-square`, the two predicate functions, and a value we'd like to insert at the end of the square.
`fits-next?` returns `true` if the left and top neighbors either don't exist, or are compatible given the predicates.
A neighbor absent in either direction by definition is compatible with any new value.

```clojure
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
```

We're almost done with the Cube Solver; let's jump back to the `solve-cube` function and then fill in the gaps.
The goal here is to use a depth-first search to find the first matching alignment of tiles; in this case,
depth-first should be faster than breadth-first since we always need the `possible-square` to fill up with every tile;
this isn't a shortest-path algorithm. So we set up a loop of `possibilities`, which is a list of tuplies of format
`[possible-square ((a1 a2 a3 a4) (b1 b2 b3 b4) (c1 c2 c3 c4))`. The first element is the `possible-square` that's been
solved thus far. The second element is a sequence of sequences, where each sub-list represents permutations of a
single tile; thus if tile `a` is a tile with 3 blue turtle heads and one green turtle tail, then `a1`, `a2`, `a3`,
and `a4` represent the four possible orientations of that tile. They remain grouped like this because each tile can
only be used once within a `possible-square`, although there are multiple ways to orient said tile.

We'll define `next-possibilities` in a moment, but let's assume that function takes in a `possible-square`, a list
of groups of permutations, and the predicates. The function will return a sequence of tuples, such that it takes all
fitting permutations of remaining tiles onto the end of the `possible-square`, and then removes all permutations of
the selected tile from the group list. If we can do that, then `solve-cube` keeps looping through the list, popping
off the first value, checking to see if it's a completed solution, and recurring through the loop with all new
possible mappings pushed onto the head of the `possibilities` list.

```clojure
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
```

Finally, let's knock out `next-possibilities`. We're going to play around with the `for` macro again. Here we
go through each group, and then pull out a single value `v` from that group. We use `:when` to check if the value
`v` could attach to the end of the `possible-square` by calling `fits-next?`, and if so, we append that value to
the end of the `possible-square`, and return a tuple of the new square with every group excluding the one with the
chosen permutation. The `for` macro returns a sequence of all such tuples.

```clojure
(defn next-possibilities [possible-square groups left-of-pred above-pred]
  (for [group groups
        v group
        :let [others (remove #(= % group) groups)]
        :when (fits-next? possible-square left-of-pred above-pred v)]
    [(append-to-square possible-square v) others]))
```

And that's it. With the mechanics of solving any square/cube out of the way, we can focus on the task at hand --
camera arrays and monsters and what not.

---

## Part 1

The data we read is again a little complex, but it's nothing we can't handle with regular expressions and our
handle `split-blank-line` function. After splitting the input into a sequence of lines of strings, we just have
to parse out the Tile ID from the first line of each sequence, and map that to a vector of the other lines of
Strings. I know I probably over-use vectors, but in this case we're going to want to look rows, columns, and 
characters by `x-y` coordinates, so I feel justified with the approach. The output of the `parse-input` function
is a sequence of tuples of ID and line vector: `([123 [line-1 line-2 line-3]], [456 [line-10 line-20 line-30]])`.

```clojure
(defn parse-tile [tile-str]
  (let [lines (str/split-lines tile-str)
        tile-num (->> (first lines)
                      (re-matches #"Tile (\d+):")
                      second
                      Integer/parseInt)]
    [tile-num (vec (rest lines))]))

(defn parse-input [input]
  (->> (utils/split-blank-line input)
       (map parse-tile)))
```

Next, we need to find all permutations of a tile. I feel no shame in reusing my own solution from
(Advent Of Code 2017 day 21)[https://github.com/abyala/advent-2017-clojure/blob/master/src/advent_2017_clojure/day21.clj]].
To do this, I define `flip` by reversing each String in the grid. Then `rotate` maps the grid 90 degrees clockwise.
And `permutations` takes the given grid and its flipped version, rotates each of them 4 times, and throws the resulting
8 values into a set just in case we have some duplicates.  Finally, `tile-permutations` calls `permutations` with its
grid component, and then reassembles the results as a vector of tiles, all with the same ID; the goal is to pass
`tile-permutations` to the `cube-solver` as the `permutation-fn`.

```clojure
(defn grid-point [grid [x y]] (get-in grid [y x]))
(defn flip [grid] (mapv str/reverse grid))
(defn rotate [grid]
  (let [max-side (dec (count grid))]
    (->> (map-indexed (fn [y row]
                        (->> (map-indexed (fn [x _]
                                            (grid-point grid [y (- max-side x)]))
                                          row)
                             (apply str)))
                      grid)
         vec)))

(defn permutations [grid]
  (->> (list grid (flip grid))
       (map #(take 4 (iterate rotate %)))
       (apply concat)
       set))

(defn tile-permutations [tile]
  (->> (second tile)
       permutations
       (map #(vector (first tile) %))
       vec))
```

Since we've got our permutation function done, it's time to define `left-of?` and `above?`. `left-of?` grabs the last
character in each row from `grid-a` and compares it against the first character in each row from `grid-b`. 
Similarly, `above?` compares the last line of `grid-a` to the first line of `grid-b`.

```clojure
(defn left-of? [[_ grid-a] [_ grid-b]]
  (= (map last grid-a)
     (map first grid-b)))

(defn above? [[_ grid-a] [_ grid-b]]
  (= (last grid-a) (first grid-b)))
```

Together, that's all we need to define a `solve` function that takes in some input and runs it through the Cube
Solver.

```clojure
(defn solve [input]
  (cube/solve-cube left-of? above? tile-permutations (parse-input input)))
```

The last thing we need to do before writing `part1` is identifying the four corner indexes of the final grid, and
that's not bad. The first index is always 0 and the last is always the final index of the collection, `(dec size)`.
The other two depend on knowing the length of the board, which is just the square root of the size of the whole board.

```clojure
(defn board-length [board] (->> board count Math/sqrt int))

(defn corner-indexes [coll]
  (let [size (count coll)
        length (board-length coll)]
    (list 0 (dec length) (- size length) (dec size))))

```

And here it is, `part1`! We solve the board, grab the tiles at each of the corners, strip away just the tile IDs,
and multiply them together.  Whew! 

```clojure
(defn part1 [input]
  (let [board (solve input)]
    (->> (map #(nth board %) (corner-indexes board))
         (map first)
         (apply *))))
```

---

## Part 2

In part 2, we're measuring the roughness of the waters, taking extra care not to disturn the local sea monsters.
Following the example from the problem statement, after removing the border around each grid in the cube, I wanted
to create a nice, simple vector of Strings without any extraneous spaces.

The code is a little tedious to go through,
but let it suffice that `strip-border` removes the top and bottom rows from a list of Strings, and then removes the
first and last character from each remaining line. Then `row-of-grids` takes in a sequence of grids (that's a 
sequence of sequence of Strings), and combines side-by-side into a single sequence of Strings. And finally,
`board-as-string` takes in the board, removes all borders, partitions together all lines that sit beside each other,
and combine it all back together into said vector of Strings.

```clojure
(defn strip-border [grid]
  (->> (butlast (rest grid))
       (map #(subs % 1 (dec (count %))))))

(defn row-of-grids [grids]
  (->> grids
       (apply interleave)
       (partition (count grids))
       (map (partial apply str))))

(defn board-as-string [board]
  (let [length (board-length board)]
    (->> board
         (map strip-border)
         (partition length)
         (map row-of-grids)
         (apply concat)
         vec)))
```

Next, I made a definition called `monster` and a function called `monster?`. The former takes in the input String
from the problem statement, and returns the set of `[x y]` coordinates that describes a sea monster. Then `monster?`
takes in a board and `x` and `y` ordinates within the board, and checks if all points relative to that `x` and `y`
line up with the sea monster.

```clojure
(def monster
  (->> (str/split-lines "                  # \n#    ##    ##    ###\n #  #  #  #  #  #   ")
       (keep-indexed (fn [y row]
                       (keep-indexed (fn [x c]
                                       (when (= \# c) [x y]) ) row)))
       (apply concat)))

(defn monster? [board x y]
  (every? (fn [[mx my]]
            (= \# (-> board (nth (+ y my) nil) (nth (+ x mx) nil))))
          monster))
```

Now that we can find a monster, we need a way to remove it from sight. Full disclosure - because I'm about to
create a `remove-monsters` function, I originally named this function `murder-monster`. But that seems violent,
so I've renamed it to `cloak-monster`. Essentially, given a `board` and `x` and `y` ordinates, we simply replace
each character that the overlapping points with a space. Clojure does well with replacing elements in a vector,
but doesn't do as well with Strings, so I created `replace-char-at` to do this manipulation. No doubt Rich Hickey
didn't put these functions in because they're wasteful, but here we are.

```clojure
(defn replace-char-at [word idx c]
  (apply str (subs word 0 idx) (str c) (subs word (inc idx))))

(defn cloak-monster [board x y]
  (reduce (fn [b [mx my]] (update b (+ y my) #(replace-char-at % (+ x mx) \space)) )
          board
          monster))
```

Now, we need to write `remove-monsters`. The goal here is to take in a board, and if we find any sea monsters in
any position, cloak them all and remove the remaining grid. However, if we don't see any sea monsters, then return
`nil` because we're probably not oriented correctly. This function leverages `loop-recur`, going through all of the
`[x y]` coordinates, along with a flag `found?` that identifies if we've seen a sea monster yet. If ay any point
`monster?` returns `true`, then cloak the monster and loop with `found?` bound to `true`. When we run out of
points to check, the return value is `(when found? board)`.

```clojure
(defn remove-monsters
  ([the-board] (remove-monsters the-board 0 0))
  ([the-board pos-x pos-y]
   (loop [board the-board x pos-x y pos-y found? false]
     (cond
       (>= y (count board))         (when found? board)
       (>= x (count (first board))) (recur board 0 (inc y) found?)
       (monster? board x y)         (recur (cloak-monster board x y) (inc x) y true)
       :else                        (recur board (inc x) y found?)))))
```

Finally, the `part2` function threads a lot of pieces together. We solve the input, extract out the grid with
`(map second tile)`, and convert it to a String using `board-as-string`. Then we reuse our handy `permutations`
function to find all possible orientations, and `keep` any non-`nil` application of `remore-monsters`. Finally,
with a monster-less grid remaining, we concatenate its Strings and count the number of waves `(\#)` in the result. 

```clojure
(defn part2 [input]
  (->> input
       solve
       (map second)
       board-as-string
       permutations
       (keep (partial remove-monsters))
       (map #(->> (apply concat %) (filter wave?) count))
       first))
```

That one was pretty rough, not so much because of the algorithm, but because the data structures were hard to hold
in the brain. Still, I had a blast with today's problem... eventually!