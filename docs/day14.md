# Day Fourteen: Docking Data

* [Problem statement](https://adventofcode.com/2020/day/14)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day14.clj)

---

Ok, I'm back to liking Advent Of Code again, after the Day That Must Not Be Named. Today we're writing
another program that acts as a program, and while not always the most challenging problems, I find
them relaxing and fun.

I gave a hint in Day 12 that whenever we see a program with an instruction set, that means the
instructions are probably going to change between parts 1 and 2, or in a future day's puzzles. Today
was no exception, so the code was structured to prepare for it.

---

## Part 1

We're introduced to a bitmask system, wherein our instruction set contains lines that set a 36-bit
bitmask, and a set of instructions on how to update memory registers. The goal is to mask the data
against the mask before setting it into the register.  When we're done, we add up the values of
registers.

First, let's get the state to pass around from instruction to instruction, which will be a map of
`:mask` as a String, and `:memory` as a map of memory address to its numeric value. Even though the
addresses look like numbers, we're not manipulating them as such, so the keys of `:memory` will be
strings.  So let's get our initializers ready.

```clojure
; State: {:mask "", :memory {"n" v}}
(def empty-mask "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
(def empty-state {:mask empty-mask :memory {}})
```

Next, let's get some helper functions to convert numeric Strings into 36-character, zero-padded
binary representations. We're mostly going to use Java's `Long` class here, first parsing the
String into a `Long`, converting it its its binary form with `toBinaryString`, and finally applying
a String formatter. Java _really_ puts up a fight with formatting numeric Strings, so I had to
pad with spaces, and then use `str/replace` to turn them into zeros. A little yucky, but that's why
we make small functions!

After that, just to keep things clear, `binary-to-long` just turns a binary string back into a `Long`.

```clojure
(defn zero-pad [s]
  (let [space-padded (->> s Long/parseLong Long/toBinaryString (format "%36s"))]
    (str/replace space-padded \space \0)))

(defn binary-to-long [b]
  (Long/parseLong b 2))
```

I then need one more function to move this along - `mask-value` takes in a numeric String value `v`
and the current `mask`, and returns the `Long` value after applying the mask. Nothing fancy here -
turn the decimal String into a binary String, overwrite all values with non-`X` mask characters,
and turn that back into a Long.

```clojure
(defn mask-value [v mask]
  (->> (zero-pad v)
       (map (fn [m v] (if (= \X m) v m)) mask)
       (apply str)
       binary-to-long))
```

I've got a cool function coming up, but for now, let's think about the two operations our input can take -
update the mask and set the bitmasked value into a register. `update-mask` and `set-bitmasked-value`
hit the spot, simply associating the correct values into the state.

```clojure
(defn update-mask [state mask]
  (assoc state :mask mask))

(defn set-bitmasked-value [state addr v]
  (assoc-in state [:memory addr] (mask-value v (:mask state))))
```

I want to add a little suspense, so let's get to the `solve` function first. Looking ahead a tiny bit
to Part 2, we learn that the `mem` command is going to be different between parts 1 and 2, so let's
figure out the overall algorithm. We're going to parse the input data and reduce the state through
each line, calling `process-line` each time. At the end, we'll grab the `:memory` of the final state,
strip away the values, and add them all together.

```clojure
(defn solve [input mem-command]
  (->> (str/split-lines input)
       (reduce #(process-line %2 %1 mem-command) empty-state)
       :memory
       vals
       (apply +)))
```

Ok, now for the fun part -- the `process-line` function. Given the current `line` of text, the `state` of
the application, and the `mem-command` (function to apply to a `mem` operation), process the line and
return the new state. For this, we want to use `re-matches` to find which regular expression matches
the line of text. There are only two possible regexes to use, but let's pretend there could be more.
In my `advent_2016_clojure` day 21 solution, I would have used a bunch of `when-let` blocks, thrown
together with an `or` to return the first non-`nil` value, as such:

```clojure
(or (when-let [args (re-matches #"regex1" line)]
      (first-function args))
    (when-let [args (re-matches #"regex2" line)]
      (second-function args)))
```

But I recently learned about the `condp` function, which is a perfect match.  It takes the form
`(condp f arg2 arg1-a fun-a arg1-b fun-b ... arg1-n fun-n)`. It applies the function `f` to
`arg1-a` and `arg2`, and if the result is non-`nil`, then it runs the next clause. If it's `nil`,
then try again with `arg1-b`. It's like a normal `condp`, but it defines the structure to apply
each time for each condition. In this case, we want to apply a different regular expression to
the `line` argument, and whichever one matches, we should apply a different function. Then `:>>`,
which I think I'll call the "double-chin," just feeds the result of the left-hand side to the 
right-hand expression.  So all together, `process-line` takes in the `line`, runs `condp` to
identify the matching regular expression, and then calls either `update-mask` or `mem-command`
on the arguments.

```clojure
(defn process-line [line state mem-command]
  (condp re-matches line
    #"mask = (\w+)" :>> (fn [[_ mask]] (update-mask state mask))
    #"mem\[(\d+)\] = (\d+)" :>> (fn [[_ addr v]] (mem-command state addr v))))
```

Last step -- `part1` calls `solve`, using `set-bitmasked-value` as the function to call when
the `line` matches the `mem` operation.

```clojure
(defn part1 [input] (solve input set-bitmasked-value))
```

---

## Part 2

As already stated earlier, part 2 is just like part 1, except that the `mem` operation now masks
the `mem` address into 1 or more addresses, and sets them all to the value on the `mem` line.
I won't go into the details of the rules, but suffice it to say we're going to define a function
called `set-bitmasked-addresses`, in contrast with part 1's `set-bitmasked-value`, to feed into
the `solve` function. Let's do this backwards, and start from the higher-order function and work
our way down.

```clojure
(defn part2 [input] (solve input set-bitmasked-addresses))
```

`set-bitmasked-addresses` needs to apply some masking function, `masked-addresses`, based on the
incoming address and the current state's mask, to get the list of "real" addresses. Then for
each one, we map the new address to the value `v` from the line. Then we update the current
state's memory by applying the `into` function to merge the old map with the new.

```clojure
(defn set-bitmasked-addresses [state addr v]
  (->> (:mask state)
       (masked-addresses addr)
       (map #(vector % (Integer/parseInt v)))
       (update state :memory (partial into))))
```

On to the `masked-addresses` function. This actually works in two steps. First, we apply the mask
to the incoming address, to come up with a new 36-character address that's made of `0`, `1`, and
`x` characters. Once we have that, we will need to explode out all of the so-called "floating"
addresses.  `masked-addresses` is nothing special - pad the input, apply the mask, reassemble
into a new String, and then delegate to the upcoming `floating-addresses` function.

```clojure
(defn masked-addresses [address mask]
  (->> (zero-pad address)
       (map (fn [m v] (if (= m \0) v m)) mask)
       (apply str)
       floating-addresses))
```

Last function coming up - `floating-addresses`. Given a String that might contain any number of `X`
characters, return all possible Strings where each `X` is replaced with a `0` or a `1`. There are
a bunch of ways to do this, but ultimately it's a choice between an iterative `loop-recur` approach
or a recursive approach. I wrote both but like using recursion in Clojure. If the function finds a
String with no `X` characters, then we return the single-element list containing that String. If
there are any `X` characters, replace the first one with a `0` and then a `1`, recursively grabbing
the `floating-addresses` with each. Then we combine the `0` list with the `1` list using `mapcat`,
which again is Clojure's flat map function.

```clojure
(defn floating-addresses [s]
  (if-not (str/index-of s \X)
    (list s)
    (mapcat #(floating-addresses (str/replace-first s \X %))
            [\0 \1])))
```

All in all, this felt like a very organized and structured little program. I dig it!