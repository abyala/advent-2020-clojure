# Day Four: Passport Processing

* [Problem statement](https://adventofcode.com/2020/day/4)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day04.clj)

---

## Part 1

In today's puzzle, we are given a data file that represents a batch of passports to validate. I think the hardest part
of the solution you'll see is that there are only so many synonyms for "validation," so it can get a little wordy.
Let's start!

As usual, the first step is to handle data validation. The input file contains a list of passports, where a passport is
a set of key-value pairs that are separated by colons. A passport may take up one or more lines, and each passport is
separated by a blank line.  As I mentioned in a previous article, I'm going to create separate functions for
`parse-input` and `parse-passport`, where the former breaks up the input file into each unparsed passport, and the
latter parses the passport itself. This also makes testing with the REPL easier.

For `parse-input`, the tricky part is dealing with the variable number of lines. There's probably a simpler solution,
but I'm not too upset with my function.  Let's go through it line by line.

***NOTE:*** The description below of `parse-input` and `parse-passport` will work and was my solution, but I've since
found a simpler solution. My source code now shows the new functions.  See the "Revisions" section at the bottom of this
document for details. So either skip these next two functions, or read them and then see how they changed below.

1. Split the input into each line, so we get a sequence of strings. The shape of the result will be
`("a" "b" "c" "" "d" "e" "")`.
2. Use `partition-by` to create groupings of lines, separated by the empty lines. `partition-by`
takes in a sequence and returns a sequence of sequences that are broken apart based on the predicate.
In this case, my predicate is `#(str/blank? %)`, which means to group together all lines until we reach an empty line;
then start a new group. The shape of the result will be `(("a" "b" "c") ("") ("d" "e") (""))`.
3. Now we need to get rid of the empty groups, since we don't need them.  A simple filter should work, and
`(filter #(not= % '("")))` does the trick. Note that we need to filter out _groups_ with a single blank string, rather
than filtering out blank strings themselves, so `(filter #(not (str/empty? %)))` is incorrect. The shape of the result
will be `(("a" "b" "c") ("d" "e"))`.
4. To finish the initial parsing, we want each passport's data to be represented as a single line, since the passport
parser shouldn't care how many lines the input originally used. `(map #(str/join " " %))` works well here, as we join
all strings into a single large string, adding in an empty string to avoid accidentally joining the last key-value pair
from one line with the first pair from the next. The shape of the result is a nice clean `("a b c" "d e")`.
5. Finally, we send each value on to the yet-unwritten `parse-passport` function.

```clojure
(defn parse-input [input]
  (->> (str/split-lines input)
       (partition-by #(str/blank? %))
       (filter #(not= '("") %))
       (map #(str/join " " %))
       (map parse-passport)))
```

Let's move on to `parse-passport`. The input line is a string of `key:value` strings, separated by spaces. A reasonable
structure for a passport would be a simple map, so this isn't too hard. First we split the line into each `key:value`
string using a space regex - `(str/split line #" ")`. Then for each substring, we can again use a regex split on the
colon character, and that will map each substring into a two-element vector. Finally, merge it all into a map.

In my original implementation, I did a single regex match on both the space and the colon, but that meant having to use
`partition` again, and I thought this is simpler to read.

```clojure
(defn parse-passport [line]
  (->> (str/split line #" ")
       (map #(str/split % #":"))
       (into {})))
```

Now we need to test if a passport is valid, which requires that it has a value for all required fields. The `cid` field
represents the Country ID which can be missing according to the instructions. So let's make a simple set of all fields
we _do_ require, and then assert that a given passport has those fields. Because the passport is just a map, we can take
all of the keys out using the `keys` function, turn that into a set, and use `set/difference` to verify that there are
no required fields that are missing from those keys.

```clojure
(def required-fields #{"byr" "iyr" "eyr" "hgt" "hcl" "ecl" "pid"})

(defn has-required-fields? [passport]
  (->> (keys passport)
       set
       (set/difference required-fields)
       empty?))
```

Ok, let's throw it all together with our `part1` function. We'll take the input string, parse it into the passports,
and count the number of them that have the required fields.

```clojure
(defn part1 [input]
  (->> (parse-input input)
       (filter has-required-fields?)
       count))
```

---

## Part 2

Oh, airport security. Thank you for taking a simple process and making it more complicated. We now need to verify each
of the fields within each passport to ensure that they are not only present, but also pass basic data validations. The
good news is that the validations are pretty straightforward.

Three validations (birth year, issue year, and expiration year) require a numeric value within a range. I don't know
for sure that the passport values will be numeric, so I wrote a little helper function called `in-range?` that tries to
parse a string as an integer, and if it works, verifies if the result is within the min and max values, inclusive. This
is the first time I'm showing Clojure's `try-catch` structure, which is very straightforward. It takes the form
`(try (expression to try) (catch ThrowableClass binding catch-expression) (finally expression))` where the `catch` and
`finally` blocks are optional. It should look just like Java, but with parentheses instead of curly braces.

Also what's neat here is the `when-let` function. Like a `let` function, it takes in a vector, but it only allows a
single binding. If the bind expression returns a non-`nil` value, then bind it and run the expression. If the bind
expression is `nil`, then skip everything else and just return `nil`.

```clojure
(defn in-range? [min max s]
  (when-let [i (try (Integer/parseInt s)
                    (catch NumberFormatException _ nil))]
    (<= min i max)))

(defn birth-year? [s] (in-range? 1920 2002 s))
(defn issue-year? [s] (in-range? 2010 2020 s))
(defn expiration-year? [s] (in-range? 2020 2030 s))
```

The hardest data validation is for height, which required a number followed by either `cm` or `in`, and different
range checks for each unit. We'll use a bunch of functions we've already seen before -- `re-matches` to attempt a 
regular expression match, `when-let` to process the resulting values if the regex matches, and then a `case` statement
to decide which min and max values to apply. We might as well use the `in-range?` function since it's already there.
Note that once again we destructure the result of `re-matches`, ignoring the first value since it's the entire String
`s`. 

```clojure
(defn height? [s] (when-let [[_ amt unit] (re-matches #"(\d+)(in|cm)" s)]
                    (case unit
                      "cm" (in-range? 150 193 amt)
                      "in" (in-range? 59 76 amt))))
```

The last three checks are much simpler. Hair color and passport ID just need regular expressions that test the shape
of the data, but don't require range checks. The functions `hair-color?` and `passport-id?` will return `nil` if
the values don't match their regexes. Clojure predicates are truthy, so any response that isn't `false` or `nil` will
resolve to `true`, which is good enough here. And `eye-color?` just ensures that the input value exists within a set; I
could test that with `(contains? the-set s)`, but calling `(the-set s)` works just as well, since it returns either `s`
if it's in the set, or `nil` if not.

```clojure
(defn hair-color?  [s] (re-matches #"\#[0-9a-f]{6}" s))
(defn eye-color?   [s] (#{"amb" "blu" "brn" "gry" "grn" "hzl" "oth"} s))
(defn passport-id? [s] (re-matches #"[\d]{9}" s))
```

Now the instructions want us to count the number of valid passports, which are "those that have all required fields and
valid values." In part 1, we only needed the number of passports with the required fields, so we've figured out the
point of reuse between parts 1 and 2 -- given a set of validations to apply, how many passports are pass those
validations? It might be easiest in this case to think about how we want the `part1` and `part2` functions to look,
as they should only differ in the validation rules to apply:

```clojure
(defn part1 [input]
  (num-valid-passports input [has-required-fields?]))
(defn part2 [input]
  (num-valid-passports input [has-required-fields? passport-passes-data-integrity?]))
```

Alright, so we need a function `num-valid-passports` that takes in an input string and a sequence of validations. I
like little functions, so we'll parse the input into its passports, filter based on those that pass an as-yet defined
function called `valid-passport?`, and then count the number of results.

```clojure
(defn num-valid-passports [input validations]
  (->> (parse-input input)
       (filter (fn [passport] (valid-passport? passport validations)))
       count))
```

On to the `valid-passport?` function. For this, we want to apply all of the `validations` we receive onto the passport,
and return true only if all validations are truthy. I could do this with a `map`, but I think `reduce` conveys our
intentions better. A passport is valid until a validation rule says otherwise, so our initialization value (second 
param) is `true`, which we then `and` with each validation check.

```clojure
(defn valid-passport? [passport validations]
  (reduce (fn [acc check] (and acc (check passport)))
          true
          validations))
``` 

Finally, let's fix up our validation functions `has-required-fields?` and `passport-passes-data-integrity?`. Sorry again
about the long names. We already implemented `has-required-fields?` in part 1, so let's look at
`passport-passes-data-integrity?`. For this, we want to look at each field within the passport, look up the data
integrity rule (if any) for that field, and return the result. If we were to have a passport with an unknown field, we
should accept it without running any validations. The easiest way to manage these rules is to map the field name to its
validation rule, and if there's no rule for a given field, then always return `true`. The `any?` function returns `true`
for any input, so that seems perfect.  We'll iterate over every element of the `passport` as a `[k v]` vector, call
`field-passes-data-integrity?`, and then assert that all results are truthy using the `every?` function.

There is one other trick here I haven't shown before. `or` is a wonderful function because it can be used both for
normal boolean operations and to return "the first non-`nil` value in a sequence." So the test function we want to apply
is either the value in the `passport-fields` map, or if there isn't one, then move on to the function `any?`. The `or`
function can take in any number of arguments, returning the first truthy value.

```clojure
(def passport-fields {"byr" birth-year?
                      "iyr" issue-year?
                      "eyr" expiration-year?
                      "hgt" height?
                      "hcl" hair-color?
                      "ecl" eye-color?
                      "pid" passport-id?})

(defn field-passes-data-integrity? [[k v]]
  (let [check (or (passport-fields k)
                 any?)]
    (check v)))

(defn passport-passes-data-integrity? [passport]
  (every? field-passes-data-integrity? passport))
```

That works! But let's make one last change - the set `required-fields` and the map `passport-fields` seem redundant.
We can remove the set and rewrite the `has-required-fields?` function to leverage the keys of the `passport-fields` map.
For each passport field name, we'll try to retrieve it from the `passport`, and then check `(every? some?)` to assert
that every mapped value has some non-`nil` value:

```clojure
(defn has-required-fields? [passport]
  (->> (keys passport-fields)
       (map #(passport %))
       (every? some?)))
```

Easy peasy!  On to day 5.

---

## Revisions Ideas

Part of the joy of Advent Of Code is reading other folks' solutions. I saw
[Nufflee's solution](https://github.com/Nufflee/AdventOfCode-2020/tree/master/day04) solution via Twitter with a Rust
solution that I think is better than my original one. I'm keeping my explanation up top for historical reasons, but
here is a much cleaner solution for parsing the input.

Instead of using `partition-by` and `filter`, the idea is that `parse-input` just has to split the giant string based
on the presence of two newlines. Now I'm coding on a Windows box, so my test String showed `\n\n` as a blank line, while
the puzzle data showed `\r\n\r\n` as a blank line, so I accommodated for that. As a result, `parse-input` gets rid of
all silly `\r` characters, splits the original line by `\n\n`, and then calls `parse-passport`. I could have replaced
the remaining `\n` characters with spaces, but instead I opted to update `parse-passport` to split the input data by
either a newline or a space.  The resulting parse logic is very clean.

One implementation note for `parse-input` - I wanted to use a threading pipeline as usual, but in this case neither
`->` nor `->>` would suffice. When calling `str/split`, the String is the first argument because the regex is second.
But when calling `map`, the threaded sequence is the last argument instead of the first. The `as->` function shines
here, since it defines a binding to apply to each step in the pipeline. So I bind `str/replace` to `x`, inject the
result as `x` as the first argument in `str/split`; and then inject the result of that, again as `x`, as the last
argument in `map`.

```clojure
(defn parse-passport [line]
  (->> (str/split line #"[\n ]")
       (map #(str/split % #":"))
       (into {})))

(defn parse-input [input]
  (as-> (str/replace input "\r" "") x
        (str/split x #"\n\n")
        (map parse-passport x)))
```