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
  (:require [clojure.string :as str]))

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

(defn clear-command
  "Builds a `clear` command"
  [var]
  (str/join ["clear " (name var)]))

(defn generate-placeholders
  "returns a lazy sequence of placeholders for a given prefix"
  [prefix]
  (map #(keyword (str/join ["__cljlab_"
                            (name prefix)
                            (str %) "__"]))
       (range)))

(defn sc
  "adds a semicolon to the end of a statement"
  [stmt]
  (str/join [stmt ";"]))
