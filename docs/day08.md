# Day Eight: Handheld Halting

* [Problem statement](https://adventofcode.com/2020/day/8)
* Solution code
** [Day 8 puzzle](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day08.clj)
** [Game Console](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/game-console.clj)

---

## Background

We knew this was coming -- the first Advent Of Code puzzle where we start building a computer.
I have no doubt that I'll be reusing this code in future puzzles, so I tried to start
building in a little code reuse, while at the same time realizing that this writeup document
will not represent what the Day 25 code will look like.

We're given a set of instructions of a program that's currently infinite looping, and we
need to show the value of the accumulator when the first instruction is hit twice. I'm
going to build out a `game-console` namespace to represent the computer, and then we'll
work on the looping logic in the `day08` namespace. Hopefully that's the right level of
separation.

---

### Game Console

First, let's represent a game console. I'll define this as a map with three elements -
the instruction offset of `:offset`, the accumulated value of `:acc`, and the current
instruction set of `:instructions`.  I fully expect future versions will both provide a
different set of instructions to apply, as well as having programs that update their own
instructions, but we'll deal with them if/when they happen.

```clojure
(defn init-console [instructions]
  {:offset       0
   :acc          0
   :instructions instructions})
```

I'll also add a little helper function to move the offset, since these projects tend to
mess around with how many instructions to jump. I don't think I've used or explained 
overloaded functions yet this year; the `move-offset` function accepts either a state 
and the number of steps to advance the offset, or just the state with an implied `1` to
move. There is a Clojure syntax for providing default argument values without overloading
the function, but I find that syntax to be super ugly to read.

```clojure
(defn move-offset
  ([state] (move-offset state 1))
  ([state n] (update state :offset + n)))
```

Now come the computer's operations, which for the time being are tiny functions. I thought
of inlining these, but again I expect a future game console to support different operations.
For the same reason, I didn't use a multi-method. Looking at each function, `op-nop` just
advances the offset, `op-acc` increments the accumulator by the argument amount and moves
the offset, and `op-jmp` moves the offset by the amount of the argument.

```clojure
(defn op-nop [state & _] (move-offset state))
(defn op-acc [state arg]
  (-> state
      (update :acc + (Integer/parseInt arg))
      move-offset))
(defn op-jmp [state arg]
  (move-offset state (Integer/parseInt arg)))
```

Then I want a function that takes in a state, runs a single operation, and returns the
next state;  we'll call this function `run-next-op`.  Again, I assume that the map of
operation name to its function will change in the future, so eventually I expect to
parameterize the map of functions. One thing that's interesting here is the expression
`(nth instructions offset nil)`. This grabs the `offset`th value (I contend that's a
real word!) out of the collection of instructions. If no such value exists, instead of
throwing an error, it will just return `nil` from the final argument. Then the
`when-let` function says that if there's no instruction at that position, just return a
`nil` for the entire `run-next-op` function, since there is no next state. This is a
more concise way of doing boundary checks without lots of extra coding.

```clojure
(defn run-next-op [{:keys [instructions offset] :as state}]
  (when-let [[ins arg] (nth instructions offset nil)]
    (let [op ({"nop" op-nop
               "acc" op-acc
               "jmp" op-jmp} ins)]
      (op state arg))))
```

---
### Part 1

Now on to the first actual challenge with the game console. We know that the program is
going to loop, and we need to capture the state when it first hits an offset it's already
processed. 

The first step is to implement the parser. There's every reason to think this will move
into the `game-console` namespace the next time we work with this code, but I'm not
confident yet that the input data will be the same. So the function `parse-input`
shouldn't be anything surprising. One important point is that we use `mapv` instead of
`map`, since a sequence of arguments in a command have an important order, so a vector
from `mapv` makes more sense than a list from `map`.

```clojure
(defn parse-input [input]
  (game/init-console (->> (str/split-lines input)
                          (mapv #(str/split % #" ")))))
```

Now we're going to implement a `run-to-completion` function to give us the final state
of the program after it's done running. Looking ahead to part 2, I already know that we
need to be able to show the conclusion of _either_ a looping progarm or one that runs to
completion, such that there is no next state. Therefore, this function will return the
completion of the run as well as how it completed. Becuase there are two possible exit
conditions, this function will return a map of
`{:status [:terminated or :loop], :state [state]}`.

My first solution attempted to use the `iterate` function again instead of `loop-recur`,
but a loop is more intuitive for this function. So what we do is loop over the current
state which we call `s`, along with a set of all offsets we've seen so far in the binding
`seen`. Within the loop body, we call `run-next-op` to calculate the next state, and then
we make a decision. If the next state is `nil`, then the program terminated, so we return
the status `:terminated` and the last state we saw. If we've already seen the next state's
offset, then the program has looped, so return a status of `:loop` the state we just 
calculated. Otherwise, the program is still running, so continue the loop.

```clojure
(defn run-to-completion [state]
  (loop [s state seen #{}]
    (let [{:keys [offset] :as next-state} (game/run-next-op s)]
      (cond
        (nil? next-state) {:status :terminated, :state s}
        (seen offset)     {:status :loop, :state next-state}
        :else             (recur next-state (conj seen offset))))))
```

Finally, all we need to do is parse the input, run it to completion, and then extract
out the accumulator from the final state.

```clojure
(defn part1 [input]
  (-> (parse-input input)
       run-to-completion
       (get-in [:state :acc])))
```

---

## Part 2

Now we need to figure out the final accumulator value when we mutate the input such that
it terminates. We don't know which line needs to change, but either a `jmp` must become
a `nop`, or the reverse is true.

First, let's calculate all possible permutations of the instructions that we may try, by
creating an `alternate-instructions` function. In here we will look at each instruction,
and sees if there's a mapping of its current operation to a new opeartion, mapping the
binding `op` to the binding `next-op`. If such a mapping exists, return the instruction 
set with the operation changed at the correct index.

There are several cool notes in this function.
1. First, remember that `keep` applies a function to a collection, filtering out the `nil` mappings.
`keep-indexed` does the same, but the function takes in the index in the collection along with the
normal collection value.
2. Second, look at the cool destructuring of the `keep-indexed` function. The function takes in the
index and the instruction, but we only care about the first value of that instruction. So the clause
`[idx [op]]` says to bind `idx` to the first argument, but since the the second argument is a sequence,
bind `op` to the first element of that sequence.
3. I mentioned this in the Day 4 problem, but the `when-let` function binds `next-op` to the expression
`({"jmp" "nop" "nop" "jmp"} op)` if it's not `nil`. This expression takes in a map that binds `jmp` to
`nop` and vice versa, and applying that map to the given operation will return either the mapping or
`nil` if it doesn't match. This is a concise way to see if a value has a mapping, check for `nil`,
and bind it all at the same time.
4. This is my first time using `assoc-in` this year. The func takes in a map, here `instructions`, a
path to an element to set, and the new value. This is a great shorthand for
`(update instructions idx (assoc 0 next-op))`. `update-in` does the same thing, except that the last
argument is the function to apply to the value already at that path, instead of `assoc-in` which
just takes in the new value.

```clojure
(defn alternate-instructions [instructions]
  (keep-indexed (fn [idx [op]]
                  (when-let [next-op ({"jmp" "nop" "nop" "jmp"} op)]
                    (assoc-in instructions [idx 0] next-op)))
                instructions))
``` 

Finally, we write `part2`. In this function, we parse the input, get all of the possible alternate
instruction set, `assoc` each one onto the `state`, and run them to completion. This will give us
back a sequence of `{:status x :state y}` maps. Then we use the familiar `keep -> when` pair of
filtering the maps for the one(s) that are terminated, returning just the accumulator. `first`
will pull out the first, and hopefully only value, and we're all done!

```clojure
(defn part2 [input]
  (let [{:keys [instructions] :as state} (parse-input input)]
    (->> (alternate-instructions instructions)
         (map #(run-to-completion (assoc state :instructions %)))
         (keep (fn [{:keys [status state]}]
                 (when (= :terminated status) (:acc state))))
         first)))
```