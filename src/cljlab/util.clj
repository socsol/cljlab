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

(defn eval-cmd
  [lab & rest]
  (b/eval lab (expr/cmd rest)))

(defn clear
  "clears variables from the lab"
  [lab & vars]
  (doall (map #(apply eval-cmd lab :clear %) vars)))

(defn eval-expr
  "calls eval on an expressions with a semi-colon at the end"
  [lab expr]
  (b/eval lab (expr/sc expr)))

(defmacro with-placeholders
  "Evaluates some forms using a set of placeholders, clears them
  afterwards and returns the result of evaluating the last form"
  [lab placeholders & forms]
  `(let [return# (do ~@forms)]
     (apply clear ~lab ~placeholders)
     return#))

(defn assign-value
  [lab placeholder val]
  (case val
    expr/all 

(defn eval-assignment-expr
  "Evaluates an assignment expression and returns the assigned values"
  [lab level out-placeholders expr]
  (if (empty? out-placeholders)
    (do
      (eval-expr lab expr)
      nil)
    (do
      (eval-expr lab expr)
      (doall (map #(b/get lab %) out-placeholders)))))

(defn call-fn-with-vars
  "Calls a function on a set of variable names that are presumed to
  be defined.  Can only return things that are handled by b/get"
  [lab level nret fn-name & vars]
  (let [out-placeholders (take nret (expr/generate-placeholders level :out))
        expr (if (zero? nret)
               (expr/placeholder-parentheses-statement fn-name vars)
               (expr/placeholder-parentheses-assignment out-placeholders fn-name vars))]

    (with-placeholders lab out-placeholders
      (eval-assignment-expr lab level out-placeholders expr))))

(defn call-fn-with-basic-vals
  "Calls a function with a set of basic parameters (i.e. things that
  can work with `b/get` and `b/set`), and returns the result, or nil"
  [lab level nret fn-name & basic-vals]
  (let [in-placeholders (take (count basic-vals) (expr/generate-placeholders level :in))]
    (with-placeholders lab in-placeholders
      (doall (map #(b/set lab %1 %2) in-placeholders basic-vals))
      (call-fn-with-vars lab level nret fn-name in-placeholders))))

(defn size
  "Returns the size of a variable in the *lab, specified by its name"
  [lab level var]
  (->> (call-fn-with-vars lab level 1 :size var)
       first
       (map int)
       vec))

(defn class
  "Returns the class of a varaible in the *lab, specified by its name"
  [lab level var]
  (->> (call-fn-with-vars lab level 1 :class var)
       first
       keyword))


(defmulti bracket-class
  "Returns the kind of brackets that should be used to address parts
  of the given structure"
  (fn [lab level var & rest] 
    (println var)
    (class lab level var)))

(defmethod bracket-class :double  [& _] :parentheses)
(defmethod bracket-class :char    [& _] :parentheses)
(defmethod bracket-class :logical [& _] :parentheses)
(defmethod bracket-class :struct  [& _] :parentheses)
(defmethod bracket-class :cell    [& _] :braces)

(defmulti get-part-basic-vars
  "Returns part of an object that can be retrieved as a basic val using basic vars"
  bracket-class)

(defn get-part-basic-vars-with-expr
  [f lab level var]
  (let [placeholder (first (expr/generate-placeholders level :get_part))]
    (with-placeholders lab [placeholder]
      (eval-assignment-expr lab level [placeholder]
                            (f [placeholder] var coords)))))

(defmethod get-part-basic-vars :parentheses
  get-part-parentheses
  [& params]
  (apply get-part-with expr/placeholder-parentheses-assignment
         params))

(defmethod get-part-basic-vars :braces
  get-part-string
  [& params]
  (apply get-part-with expr/placeholder-braces-assignment
         params))
