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

(ns cljlab.core
  (:refer-clojure :exclude [class eval get set type])
  (:require [clojure.string :as str]
            [cljlab.basic :as b]
            [cljlab.expr :as expr]
            [cljlab.util :as util]))

;; These are imported from the "basic" namespace
(def open b/open)
(def exit b/exit)
(def disconnect b/disconnect)
(def type b/type)
(def interface b/interface)
(def open? b/open?)
(def eval b/eval)

(def all util/all)
(def end util/end)

(defn class [lab & params]
  (apply util/class lab 0 params))

(defn size [lab & params]
  (apply util/size lab 0 params))

(defmulti get-with-level
  "Used internally to retrieve multi-level datastructures"
  util/class)

(defn get [lab & params]
  "Returns the value of a *lab variable mapped to Clojure datastructures"
  (apply get-with-level lab 0 params))

(defmethod get-with-level :double
  get-double
  [lab level var]
  (let [raw (b/get lab var)
        dims (size lab level var)
        ndims (count dims)]
    (cond
     ;; a single item
     (every? #(= 1 %) dims) (first raw)

     ;; a single row
     (and (= ndims 2)
          (= (first dims) 1)) (vec raw)

     ;; otherwise, need to construct a nested row-major array.
     :else (let [sep (reduce * (butlast dims))]
             ((fn recurse-array [position array-level]
                (if (= array-level (dec ndims))
                  ;; Make a single len-long vector containing every sep'th
                  ;; item
                  (vec (take-nth sep (drop (reduce + position) raw)))

                  ;; Make (nth dims-reordered level)-many sub-seqs
                  (vec (map #(recurse-array (conj position %) (inc array-level))
                            (range (nth dims array-level))))))
              [] 0)))))

(defmethod get-with-level :char
  get-char
  [lab level var]
  (let [dims (size lab level var)
        ndims (count dims)
        rows (first dims)]
    (cond

     ;; a plain string -- passthrough
     (and (= ndims 2)
          (= rows 1)) (b/get lab level var)

     ;; otherwise, get each slice
     :else (map #(util/get-part lab level var [% all]) (range rows)))))

(defn set
  "Sets the value of a *lab variable using a reverse of the mapping used by `get`"
  [lab variable value]
  nil)
