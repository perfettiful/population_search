(ns simple-search.core
  (:use simple-search.knapsack-examples.knapPI_11_20_1000
        simple-search.knapsack-examples.knapPI_13_20_1000
        simple-search.knapsack-examples.knapPI_16_20_1000))

;;; An answer will be a map with (at least) four entries:
;;;   * :instance
;;;   * :choices - a vector of 0's and 1's indicating whether
;;;        the corresponding item should be included
;;;   * :total-weight - the weight of the chosen items
;;;   * :total-value - the value of the chosen items

(defn included-items
  "Takes a sequences of items and a sequence of choices and
  returns the subsequence of items corresponding to the 1's
  in the choices sequence."
  [items choices]
  (map first
       (filter #(= 1 (second %))
               (map vector items choices))))

(defn random-answer
  "Construct a random answer for the given instance of the
  knapsack problem."
  [instance]
  (let [choices (repeatedly (count (:items instance))
                            #(rand-int 2))
        included (included-items (:items instance) choices)]
    {:instance instance
     :choices choices
     :total-weight (reduce + (map :weight included))
     :total-value (reduce + (map :value included))}))


;;  (println (random-answer knapPI_16_20_1000_1))

;;; It might be cool to write a function that
;;; generates weighted proportions of 0's and 1's.

(defn score
  "Takes the total-weight of the given answer unless it's over capacity,
  in which case we return 0."
  [answer]
  (if (> (:total-weight answer)
         (:capacity (:instance answer)))
    0
    (:total-value answer)))

(defn add-score
  "Computes the score of an answer and inserts a new :score field
  to the given answer, returning the augmented answer."
  [answer]
  (assoc answer :score (score answer)))

(defn random-search
  [instance max-tries]
  (apply max-key :score
         (map add-score
              (repeatedly max-tries #(random-answer instance)))))


(defn find-answer
  [choice instance]
  (let [choices choice
        included (included-items (:items instance) choices)]
    {:instance instance
     :choices choices
     :total-weight (reduce + (map :weight included))
     :total-value (reduce + (map :value included))}))

(defn tweak-choice
  [choice x]
  (let [conchoice (into [] choice)]
    (if (> x 0)
      (tweak-choice (assoc conchoice (rand-int (count conchoice)) (rand-int 2)) (dec x))
      (seq choice)
      )))


(defn hill-climb
  [winner max-tries max-tries-perhill instance]
  (loop [num-tries 0
         current-best winner
         previous-hill winner]
    (if (>= num-tries max-tries)
      (max-key :score current-best previous-hill)
      (if(=(mod num-tries max-tries-perhill) 0)

        (let [tweaked-choices (tweak-choice (:choices current-best)  10)
              new-answer (find-answer tweaked-choices instance)
              scored-new-answer (add-score new-answer)]
          (recur (inc num-tries)
                 (add-score (random-answer instance))
                 (max-key :score current-best previous-hill)))

        (let [tweaked-choices (tweak-choice (:choices current-best)  10)
              new-answer (find-answer tweaked-choices instance)
              scored-new-answer (add-score new-answer)]
          (recur (inc num-tries)
                 (max-key :score current-best scored-new-answer)
                 previous-hill))
        ))))


(hill-climb (add-score (random-answer knapPI_11_20_1000_29)) 20000 2000 knapPI_11_20_1000_29)
