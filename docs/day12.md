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

```clojure
(defn part1 [input]
  (->> (reduce #(next-state %1 %2)
               [[0 0] :east]
               (str/split-lines input))
       first
       (mapv utils/abs)
       (apply +)))
```

---

## An Interlude before Part 2

Part 2 says that the instructions we took for most of the operations in part 1 were wrong, and now we
need to change how our program works. (Helpful tip - when the problem statement says "you work out
what [the commands] probably mean," that means they're going to change in part 2!) We learn that our
ship has a waypoint that does most of the movement. The waypoint is what moves north, south, east,
and west on `N`, `S`, `E`, and `W`. And instead of the ship turning left or right, the waypoint revolves
around the ship by a number of degrees. Finally, when the ship goes forward on an `F` command, it moves
`n` times to the relative position of the waypoint to the ship.

Here's the fun realization - we can implement the ship's direction from part 1 as the waypoint's position
from part 2. In part 1, we know the ship starts off facing east. If the first command were to be `F10`,
then it would end up moving from `[0 0]` to `[10 0]`. Now another way to think of "facing East" as "has
a waypoint of `[1 0]`." If the waypoint were at `[1 0]` and we moved toward it 10 times because of `F10`,
we would still end up at position `[10 0]`. Likewise, if the ship faces east and rotates 90 degrees to
the right, it would be facing south. Or stated different, if a waypoint at `[1 0]` rotates 90 degrees,
remembering that North is negative and South is positive, then the waypoint would end up at `[0 1]`.
And from that position, moving forward 10 positions would have the same effect whether going 10 South or
moving 10 steps toward `[0 1]`.

So let's figure out what's really different between parts 1 and 2. In part 1, `N`, `S`, `E`, and `W`
moves the ship in that direction, while in part 2 it moves the waypoint. In both parts, `L` and `R`
can be seen as rotating the waypoint around the ship, and `F` can be seen as moving toward the waypoint,
so long as we initialize the waypoint correctly.

Therefore, the two differences between parts 1 and 2 are
1. The initial state of the waypoint (`[1 0]` for part 1 and `[10 -1]` for part 2), and
2. What moves during a directional movement.

Ok, let's refactor!

---

## Part 2

First of all, let's change the shape of our data. Instead of holding on to an `[x y]` point and a direction,
a ship's state will be defined as the ship's position and the waypoint's position. This time, I'll represent
it as a map, so our state will be of shape `{:ship [x y], :waypoint [x y]}`. We can initialize the state
with the initial position of the waypoint.

```clojure
(defn new-ship [initial-waypoint]
  {:ship [0 0] :waypoint initial-waypoint})
```

We'll keep the old var `dir-amounts` from the initial implementation, and that still maps each direction
to the `[dx dy]` amount we move in that direction.

Next, let's get two functions that deal with movement -- `slide` and `follow-waypoint`. I named the first one
`slide` because in part 1, a ship facing East and traveling North is somehow magically travelling laterally,
so it feels like it's slipping. In part 1, the ship will slide, and in part 2 the waypoint will. To do this,
we'll `update` the state, multiplying the instruction's amount by the `dir-amount`, and adding that to the
current value in the state. For `follow-waypoint`, we do the same thing, except we multiply the instruction's
amount by the waypoint's data instead. Since these two commands are very similar, both `slide` and
`follow-waypoint` delegate their work to a common function called `move`. `move` accepts the current state;
the `mover`, either `:ship` or `:waypoint`, to determine what's going to change; the `target` as the `[dx dy]`
point to work with; and the `amt` to multiply the `target`. Since we can `slide` either the ship or the
waypoint, we feed that value in to the function, but `follow-waypoint` always moves the ship, so we can
hard-code that value.

```clojure
(defn move [state mover target amt]
  (let [move-by (mapv * target [amt amt])]
    (update state mover (partial mapv + move-by))))

(defn slide [state mover dir amt]
  (move state mover (dir-amounts dir) amt))

(defn follow-waypoint [state amt]
  (move state :ship (state :waypoint) amt))
```

Now we have to work on rotating the waypoint, and again we remember that we only receive multiples of 90.
The easiest way to handle rotations is to do so iteratively, since the math can be a little tedious to 
work with if we try to do it in one step. To build out `rotate-waypoint`, I first bind `times` to the 
number of 90-degree clockwise rotations we need to do. The function will accept counter-clockwise 
rotations as negative numbers, since turning left 90 degrees is the same as turning right -90 degrees.
We `mod` the degrees by 360 to get a value in `#{0, 90, 180, 270}`, and then take the quotient from 90
to get the number of rotations between 0 and 3. Then for clarity, I define a helperful function `rotate90`,
which takes in a point and rotates it once by setting `[x' y'] = [-y x]`. Hooray for paper math. Then
instead of a `loop`, I combined `iterate` with `nth` to complete the function.

```clojure
(defn rotate-waypoint [state degrees]
  (let [times (-> degrees (mod 360) (quot 90))
        rotate90 (fn [[x y]] [(- y) x])]
    (-> (iterate
          (fn [s] (update s :waypoint rotate90))
          state)
        (nth times))))
```

We're almost done.  The `next-state` function looks very similar to what we saw before, except the
`case` expression leverages the new helper functions. Again, `mover` is a parameter that's set to
`:ship` for part 1 and `:waypoint` for part 2. So `N`, `S`, `E`, and `W` slides the `state` based on
the `mover`; `L` and `R` rotates the waypoint; and `F` moves the ship toward the waypoint. Everything
is reused.

```clojure
(defn next-state [state mover line]
  (let [op (first line)
        amt (-> (subs line 1) Integer/parseInt)]
    (case op
      \N (slide state mover :north amt)
      \S (slide state mover :south amt)
      \E (slide state mover :east amt)
      \W (slide state mover :west amt)
      \L (rotate-waypoint state (- amt))
      \R (rotate-waypoint state amt)
      \F (follow-waypoint state amt))))
```

All that's left is the `solve` function and the tiny `part1` and `part2` functions. `solve` looks
almost identical to what we used to do in `part1` - split the input data, initialize the ship with
the starting waypoint, reduce the data using the `next-state` function. Then from the resulting
state, retrieve the point that's associated to `:ship`, and calculate the Manhattan Distance using
absolute value and addition.  Then we call this function using `[1 0]` (due East) and `:ship` for
part 1, and using `[10 -1]` (10 East, 1 North) and `:waypoint` for part 2.

```clojure
(defn solve [initial-waypoint mover input]
  (->> (reduce #(next-state %1 mover %2)
               (new-ship initial-waypoint)
               (str/split-lines input))
       :ship
       (mapv utils/abs)
       (apply +)))

(defn part1 [input]
  (solve [1 0] :ship input))

(defn part2 [input]
  (solve [10 -1] :waypoint input))
```