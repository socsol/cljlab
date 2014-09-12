;; Copyright 2014 Alastair Pharo

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;;     http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns cljlab.util
  (:refer-clojure :exclude [class])
  (:require [cljlab.basic :as b]
            [cljlab.expr :as expr]))

(defn clear
  "clears variables from the lab"
  [lab & vars]
  (doall (map #(b/eval lab (expr/clear-command %)) vars)))

(defn eval-expr
  "calls eval on an expressions with a semi-colon at the end"
  [lab expr]
  (b/eval lab (expr/sc expr)))

(defmacro with-placeholders
  "Performs some function using a set of placeholders, clears them
  afterwards and returns the result of the function"
  [lab placeholders & forms]
  `(let [return# (do ~@forms)]
     (apply clear ~lab ~placeholders)
     return#))

(defn call-basic
  "Calls a function with a set of basic parameters (i.e. things that
  can work with `get` and `set`), and returns the result, or nil"
  [lab nret fn-name & params]
  (let [generate (fn [label n] (take n (expr/generate-placeholders label)))
        in-placeholders (generate :in (count params))
        out-placeholders (generate :out nret)]

    (with-placeholders lab (flatten [in-placeholders out-placeholders])
      (doall (map #(b/set lab %1 %2)
                  in-placeholders
                  params))
      (if (zero? nret)
        (do
          (eval-expr lab (expr/placeholder-parentheses-statement fn-name in-placeholders))
          nil)
        (do
          (eval-expr lab (expr/placeholder-parentheses-assignment out-placeholders fn-name in-placeholders))
          (doall (map #(b/get lab %)
                      out-placeholders)))))))

(defn size
  "Returns the size of a variable in the *lab, specified by its name"
  [lab var]
  (->> (call-basic lab 1 :size [var])
       first
       (map int)
       vec))

(defn class
  "Returns the class of a varaible in the *lab, specified by its name"
  [lab var]
  (->> (call-basic lab 1 :class [var])
       first
       keyword))

;; These are used to refer to parts of a matrix, cell, etc.
(def end :end)
(def all ":")

(defmulti bracket-class
  "Returns the kind of brackets that should be used to address parts
  of the given structure"
  (fn [lab var & rest] (class lab var)))

(defmethod bracket-class :double [] :parentheses)
(defmethod bracket-class :char [] :parentheses)
(defmethod bracket-class :logical [] :parentheses)
(defmethod bracket-class :struct [] :parentheses)
(defmethod bracket-class :cell [] :braces)

(defmulti get-part
  (fn [lab var & rest] (bracket-class lab var)))

(defn get-part-with
  [f lab var coords]
  (let [placeholder (first (expr/generate-placeholders :get_part))]
    (with-placeholders lab [placeholder]
      (eval-expr lab (f [placeholder]
                        var
                        coords))
      (b/get lab placeholder))))

(defmethod get-part :parentheses
  get-part-parentheses
  [& params]
  (apply get-part-with expr/placeholder-parentheses-assignment
         params))

(defmethod get-part :braces
  get-part-string
  [& params]
  (apply get-part-with expr/placeholder-parentheses-assignment
         params))
