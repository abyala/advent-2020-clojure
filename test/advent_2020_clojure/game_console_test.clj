(ns advent-2020-clojure.game-console-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.game-console :refer :all]))

(deftest move-offset-test
  (is (= {:offset 2 :acc 0 :instructions nil}
         (move-offset {:offset 1 :acc 0 :instructions nil})))
  (is (= {:offset 6 :acc 0 :instructions nil}
         (move-offset {:offset 1 :acc 0 :instructions nil} 5))))

(deftest op-nop-test
  (is (= {:offset 2 :acc 0 :instructions nil}
         (op-nop {:offset 1 :acc 0 :instructions nil} "+3"))))

(deftest op-acc-test
  (is (= {:offset 2 :acc 3 :instructions nil}
         (op-acc {:offset 1 :acc 0 :instructions nil} "+3")))
  (is (= {:offset 2 :acc -7 :instructions nil}
         (op-acc {:offset 1 :acc 8 :instructions nil} "-15"))))

(deftest op-jmp-test
  (is (= {:offset 4 :acc 0 :instructions nil}
         (op-jmp {:offset 1 :acc 0 :instructions nil} "+3")))
  (is (= {:offset 3 :acc 0 :instructions nil}
         (op-jmp {:offset 8 :acc 0 :instructions nil} "-5"))))