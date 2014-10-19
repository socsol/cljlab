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

(ns cljlab.expr
  (:refer-clojure :rename {name core-name})
  (:require [clojure.string :as str]))

;; These are used to refer to parts of a matrix, cell, etc.
(def end "end")
(def all ":")

(defn name
  "A stand-in for clojure.core/name that leaves numbers alone"
  [thing]
  (if (number? thing)
    (str thing)
    (core-name thing)))

(defn placeholder-assignment
  "Evaluates an assignment involving placeholders"
  [lvals rval]
  (str/join [ "["
              (str/join "," (map name lvals))
              "] = " (name rval) ]))

(defn placeholder-bracket-statement
  "Builds a string for an RHS statement involving brackets"
  [left-bracket right-bracket val params]
  (str/join [(name val)
             left-bracket
             (str/join "," (map name params))
             right-bracket]))

(defn placeholder-parentheses-statement
  "Builds an statement involving parentheses `(` `)` but not assignment"
  [& rest]
  (apply placeholder-bracket-statement "(" ")" rest))

(defn placeholder-braces-statement
  "Builds an statement involving braces `{` `}` but not assignment"
  [& rest]
  (apply placeholder-bracket-statement "{" "}" rest))

(defn placeholder-bracket-assignment
  "Builds an assignment involving placeholders and brackets"
  [lvals & rest]
  (placeholder-assignment lvals
                          (apply placeholder-bracket-statement rest)))

(defn placeholder-parentheses-assignment
  "Builds an assignment involving placeholders and parentheses - `(` and `)`"
  [lvals & rest]
  (apply placeholder-bracket-assignment lvals "(" ")" rest))

(defn placeholder-braces-assignment
  "Builds an assignment involving placeholders and braces - `{` and `}`"
  [lvals & rest]
  (apply placeholder-bracket-assignment lvals "{" "}" rest))

(defn cmd
  "Builds a command"
  [command & params]
  (str/join " " (cons (name command) (map name params))))

(defn generate-placeholders
  "returns a lazy sequence of placeholders for a given prefix"
  [level prefix]
  (map #(keyword (str/join ["cljlab__"
                            (str level)
                            "__"
                            (name prefix)
                            (str %) "__"]))
       (range)))

(defn sc
  "adds a semicolon to the end of a statement"
  [stmt]
  (str/join [stmt ";"]))
