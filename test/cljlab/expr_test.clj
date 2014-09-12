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

(ns cljlab.expr-test
  (:use midje.sweet)
  (:require [cljlab.expr :as expr]))

(fact "`placeholder-assignment` correctly assembles assignment statements involving placeholders"
      (expr/placeholder-assignment [:a :b] :c) => "[a,b] = c"
      (expr/placeholder-assignment [:__cljlab_out1__ :__cljlab_out2__] :__cljlab_in__) => "[__cljlab_out1__,__cljlab_out2__] = __cljlab_in__")

(fact "`placeholder-bracket-statement` returns a correctly formatted bracket statement"
      (expr/placeholder-bracket-statement "(" ")" :a [:b :c :d :e]) => "a(b,c,d,e)"
      (expr/placeholder-bracket-statement "{" "}" :a1 [:a2 :a3 :a4 :a5]) => "a1{a2,a3,a4,a5}")

(fact "`placeholder-parentheses-statement` returns a correctly formatted bracket statement"
      (expr/placeholder-parentheses-statement :a [:b :c :d :e]) => "a(b,c,d,e)"
      (expr/placeholder-parentheses-statement :a1 [:a2 :a3 :a4 :a5]) => "a1(a2,a3,a4,a5)")

(fact "`placeholder-bracket-assignment` correctly assembles assignment statements involving placeholders"
      (expr/placeholder-bracket-assignment [:a :b] "(" ")" :c [:d :e]) => "[a,b] = c(d,e)"
      (expr/placeholder-bracket-assignment [:__cljlab_out1__ :__cljlab_out2__] "{" "}" :__cljlab_in__ [":" :y]) => "[__cljlab_out1__,__cljlab_out2__] = __cljlab_in__{:,y}")

(fact "`placeholder-parentheses-assignment` correctly assembles assignment statements involving placeholders"
      (expr/placeholder-parentheses-assignment [:a :b] :c [:d :e]) => "[a,b] = c(d,e)"
      (expr/placeholder-parentheses-assignment [:__cljlab_out1__ :__cljlab_out2__] :__cljlab_in__ [":" :y]) => "[__cljlab_out1__,__cljlab_out2__] = __cljlab_in__(:,y)")

(fact "`placeholder-braces-assignment` correctly assembles assignment statements involving placeholders"
      (expr/placeholder-braces-assignment [:a :b] :c [:d :e]) => "[a,b] = c{d,e}"
      (expr/placeholder-braces-assignment [:__cljlab_out1__ :__cljlab_out2__] :__cljlab_in__ [":" :y]) => "[__cljlab_out1__,__cljlab_out2__] = __cljlab_in__{:,y}")

(fact "`clear-command` correctly builds a `clear` command"
      (expr/clear-command :a) => "clear a"
      (expr/clear-command :testing123) => "clear testing123"
      (expr/clear-command :__cljlab_in*__) => "clear __cljlab_in*__")

(fact "`generate-placeholders` creates a lazy seq of placeholder keywords"
      (seq? (expr/generate-placeholders :a)) => truthy
      (take 5 (expr/generate-placeholders :a)) => (list :__cljlab_a0__ :__cljlab_a1__ :__cljlab_a2__ :__cljlab_a3__ :__cljlab_a4__))

(fact "`sc` adds a semicolon to the end of a string"
      (expr/sc "test") => "test;")
