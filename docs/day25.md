# Day Twenty-Five: Combo Breaker

* [Problem statement](https://adventofcode.com/2020/day/25)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day25.clj)

---

After 24 days of code hacking, our final puzzle is a little password hacking. True to form, the final day's
puzzle is pretty simple, I assume because the coordinator of Advent Of Code wants to dissuade folks from
banging our heads against the wall on Christmas day.

---

## Part 1

There's not much to say here. We're given a cryptographic transformation algorithm that our private keys
operate on a number of times to generate some output. Two systems, a key card and a door, both apply the
cryptographic transformation on a common, widely-known secret to generate their public keys, which we are
given as input. Then both the card and the door use their own private key (the looping factor) on the other's
public key to generate the answer, ostensibly the shared encryption key.  Because both the card and the door
should generate the same encryption key, we only have to calculate it for either the card or the door, but my
test case asserts that the two are equal.

Because cracking the private key and generating the encryption key use the same transformation algorithm,
I created `crypto-transform`, which generates an infinite sequence of transformation on its `subject-number`
input. On each iteration, we take the previous value (starting with `1`), multiply it by the `subject-number`,
and take the remainder after dividing by `20201227`.

```clojure
(defn crypto-transform [subject-number]
  (iterate #(-> (* % subject-number) (rem 20201227)) 1))
```

Then to crack the private key, we need to figure out how many loops through the transformation function it
takes to generate the public key. We just use `keep-indexed` to find the first iteration when the output
equals the public key, pulling out the index that represents the iteration number.

```clojure
(defn crack-private-key [subject-number public-key]
  (->> (crypto-transform subject-number)
       (keep-indexed (fn [idx v] (when (= v public-key) idx)))
       first))
```

Finally, given the two public keys, we can determine the encryption key, which is the goal of part 1.
We start with the sequence of transformations of the second public key, looping the number of times equal
to the first private key, which we get by calling `crack-private-key`.  And again, the test code verifies
this, rather than the application code, but `(part1 key1 key2)` equals `(part1 key2 key1)`.

```clojure
(defn part1 [key1-pub key2-pub]
  (->> (crypto-transform key2-pub)
       (drop (crack-private-key 7 key1-pub))
       first))
```

--- 

## Part 2

There is no part 2.  By finishing the first 49th stars, we get the 50th for free.  Congratulations to all
who participated in Advent Of Code this year!  And a huge thanks to [@ericwastl](https://twitter.com/ericwastl)
for putting together yet another fantastic coding event!