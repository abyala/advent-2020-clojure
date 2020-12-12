# Day Twelve: Rain Risk

* [Problem statement](https://adventofcode.com/2020/day/12)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day12.clj)

---

I liked today's puzzle, because it became clear after implementing Part 1 that there's a logical
rewrite to do to make both parts look the same.  This time around, I'm going to provide my solution
to part 1 without knowing anything about part 2, and then we'll do a refactoring.

---

# Part 1

In this part, we are given a list of instructions and are given instructions on how to steer the
ship. Each instruction has a single-character operation, following by a numeric argument.
We move north, south, east, or west _without changing direction_ if the operation is `N`, `S`, `E`,
or `W`. We turn the ship without moving it counter-clockwise or clockwise, `L` or `R`, by the number
of degrees; note that we only get multiples of 90 to keep the math simple. And finally, we can move
the ship forward, `F`, in the direction it's currently facing.

The first part I want to solve is how to rotate the ship with the `L` and `R` instructions. I 
define a vector of all possible directions going in a clockwise motion. Then in the `rotate`
function, the goal is to find the index of the current direction, and add `(quot amt 90)` to the
index, since each 90 degrees will move us to the next direction in the circle. We use
`(mod (count dirs))` to wrap around the edges, and then we grab the direction at the new index.

Note that there's another way to write this function. After finding the index and the number of steps
to move, instead of using `mod`, we can just find the `nth` location in an infinite cycle of the
original direction list.  It's less efficient if for some reason we had to rotate a 9 million degrees,
but it's a neat option.

```clojure
(def dirs [:north :east :south :west])
(defn rotate [dir amt]
  (-> (.indexOf dirs dir)
      (+ (quot amt 90))
      (mod (count dirs))
      dirs))

; inefficient alternative
(defn rotate [dir amt]
  (->> (.indexOf dirs dir)
       (+ (quot amt 90))
       (nth (cycle dirs))))
```

Let's take a quick moment to think about the state we're going to use. A ship is represented by two
pieces of data - the current `[x y]` coordinates and the direction we're facing. I could use a map
of these values, or a 3-element vector, but a two element vector of the `[x y]` pair and the direction
seemed simple enough. So the starting position of facing east will be `[[0 0] :east]`. 

Now, let's figure out how to move the ship without rotating it.  We'll write a function called `move`,
which takes in the current position, direction to move, and the amount to move. Just as we made a 
vector of all directions, we'll make a map of each direction to the vector we'll add to move once in
that direction. Most AoC functions assume that the origin is in the top-left corner of the input and,
`x` moves to the right while `y` moves _down_. It's strange but I've become used to it, but it does
mean that north is `[0 -1]` instead of a more traditional `[0 1]`.

To move, we find the amount to move from the `dir-amounts` map, multiply the `[dx dy]` pair by the 
`amt` value to know how far we need to move in total, and then use `(mapv + [x y]`) to add it to the
current position of the ship.

```clojure
(def dir-amounts {:north [0 -1]
                  :south [0 1]
                  :east  [1 0]
                  :west  [-1 0]})

(defn move [[[x y] dir] amt]
  (->> (dir-amounts dir)
       (mapv * [amt amt])
       (mapv + [x y])))
```

Then we create the `next-state` function to move the state based on the current line in the instructions.
We'll destructure the heck ouf of the input again, reading in `state` as `[[x y] dir :as state]` to pull
apart the values and keep the state intact at the same time. Then we parse the `op` and `amt` out of the
instruction, and use `case` to switch based on the `op`. For each value, I just manually reconstruct the
vector by manually changing the point. It's not clean, but again we're about to rewrite this.

```clojure
(defn next-state [[[x y] dir :as state] line]
  (let [op (first line)
        amt (-> (subs line 1) Integer/parseInt)]
    (case op
      \N [[x (- y amt)] dir]
      \S [[x (+ y amt)] dir]
      \E [[(+ x amt) y] dir]
      \W [[(- x amt) y] dir]
      \L [[x y] (rotate dir (- amt))]
      \R [[x y] (rotate dir amt)]
      \F [(move state amt) dir])))
```

Finally, the `part1` function uses the `reduce` function on the starting state, calling `next-state` to
process each line. Then when we're done, we grab the `[x y]` coordinates out of the state using `first`,
and calculate the Manhattan distance by taking the absolute value of `x` and `y` and adding them.

