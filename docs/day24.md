# Day Twenty-Four: Lobby Layout

* [Problem statement](https://adventofcode.com/2020/day/24)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day24.clj)

---

Today we're playing with flipping hexagons upside down. As with [day 11](day11.md) and [day 17](day17.md),
we're given some kind of grid and rules for turning points on and off. The good news is that I enjoy these
problems, I was happy to have one more!

Before going through the code, I should note that we're working with hexagonal tiles on a floor, which means
we need to think about how to represent the coordinates of each hex. We already know that the directions
we'll use are east, southeast, southwest, west, northwest, and northeast, so our hexagons have points at the
top and bottom, and edges on the left and right sides. I like to think that the hex directly to the east of
`[0 0]` is at `[2 0]`, because that means the hex to the northeast is at `[1 1]`. By using this kind of
coordinate system, we can always see some kind of `x` or `y` movement whenever moving tiles, and we don't
have to work with fractions.  With that out of the way, let's get to work!

---

## Part 1

In part 1, we're given as input a list of Strings, which represents the path from the origin to a final tile.
Every tile starts off with the white side facing up, but we flip the tiles when it's at the end of a path.
Since we can flip tiles multiple times, our goal is to count the number of them that end up being black.

So first, let's set up the directions and their relative coordinates, as described in the preamble. Now
I could have saved a few keystrokes by mapping String values instead of keywords, but that doesn't seem very
Clojure-y, and the transformation cost is low.

```clojure
(def directions {:e [ 2 0] :ne [ 1 1] :se [ 1 -1]
                 :w [-2 0] :nw [-1 1] :sw [-1 -1]})
```

Then let's parse the data, and a regular expression handles this beautifully. We know that every step has
either an east or west component, and may optionally have a north or south component, so the regex
`[n|s]?[e|w]` covers our needs. Once we have our word, I transform it into a keyword, as described above,
so we can use it with the `directions` map.

```clojure
(defn parse-path [input]
  (->> input (re-seq #"[n|s]?[e|w]") (map keyword)))
```

So now we need to figure out how to walk a path of direction keywords, and this is easily accomplished
with a simple `reduce` function. First we'll map each of the direction keywords into their `[x y]`
offsets, and then starting from the origin `[0 0]`, we use `(mapv +)` to add the current `x` to the offset
`x`, and the current `y` to the offset `y`.

```clojure
(defn tile-at [path]
  (reduce #(mapv + %1 %2)
          [0 0]
          (map #(directions %) path)))
```

The function `initial-black-tiles` takes in the input String and returns all tiles that end up black
after however many flips. To do this, we start off mapping each line into its parsed path, and then its
final tile location using `tile-at`, which gives us a sequence of tile coordinates. We then use
`frequencies` to return a tuple of `[[x y] frequencies]` that represents how many times each point was
flipped over. To find the black tiles, given all tiles start off white, we just need the set of tiles
whose flip frequency is odd.

```clojure
(defn initial-black-tiles [input]
  (->> (str/split-lines input)
       (map (partial (comp tile-at parse-path)))
       frequencies
       (keep (fn [[tile freq]] (when (odd? freq) tile)))
       (into #{})))
```

Finally, for part 1 we just count up the number of black tiles.

```clojure
(defn part1 [input]
  (-> input initial-black-tiles count))
```

---

## Part 2

In this part, we're doing another evolutionary game program, where every point decides whether to flip to
black or white based on its current state and that of its neighbors. As we've seen before, we'll need to
decide which tiles to examine, and what the next state of each of these tiles is, and then an `iterate`
function to go through the turns.

First, let's handle finding all tiles adjacent to a given tile. We've already seen in `tile-at` that
we use `(mapv + tile1 tile2)` to add together two points, so we just need to take all of the directional
`[x y]` coordinates and add them to the tile.

```clojure
(defn adjacencies [tile]
  (->> (vals directions)
       (map #(mapv + tile %))))
``` 

Next, we'll define `next-side-black?` to see if a tile should be black in the next turn, given the set
of tiles currently black. To determine this, we'll see how many adjacent tiles are black by finding the
adjacencies, filtering for those in the set of black tiles, and counting them up. Then we apply different
rules based on whether the current tile is black or white to return the boolean response.

```clojure
(defn next-side-black? [tile black-tiles]
  (let [black-adjacent (->> (adjacencies tile)
                            (filter #(black-tiles %))
                            count)]
    (if (black-tiles tile)
      (#{1 2} black-adjacent)
      (= 2 black-adjacent))))
```

Now we're ready to create the `next-turn` function, which takes in a set of black tiles in one generation,
and returns the set of black tiles in the next generation, and there's a tiny tricky part to this function.
First, we use `(mapcat adjacencies tile-set)` to get all of tiles that are adjacent to all of the currently
black tiles; remember that we use `mapcat` as Clojure's flat map function, since we want a single-level
sequence of tiles. The thing is, we also need to consider the tiles that are currently black, not just the
neighbors of the black tiles, so we take the above sequence and call `(into tile-set)` to add them to the
current black tiles. This gives us the set of all tiles we need to consider in the next round. With that
out of the way, we again filter each considered tile based on whether it will be black in the next turn,
and turn the resulting sequence into a set.

```clojure
(defn next-turn [tile-set]
  (->> tile-set
       (mapcat adjacencies)
       (into tile-set)
       (filter #(next-side-black? % tile-set))
       set))
```

Finally, we just have to see the outcome after 100 turns. We'll take the initial black tiles and feed it
into `(iterate next-turn)` to get the sequence of black tile sets after each turn. `iterate` returns the
initial input as the head of its output sequence, so to get to the 100th turn, we need to `drop` 100
values instead of 99.  Then we simply count the number of black tiles in the next value and we're done.

```clojure
(defn part2 [input]
  (->> (iterate next-turn (initial-black-tiles input))
       (drop 100)
       (map count)
       first))
```
