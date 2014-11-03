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

(def all expr/all)
(def end expr/end)

(def get-basic b/get)
(def set-basic b/get)

(defn class [lab & params]
  (apply util/class lab 0 params))

(defn size [lab & params]
  (apply util/size lab 0 params))

(defmulti get-with-level
  "Used internally to retrieve multi-level datastructures"
  util/class)

(defn get
  "Returns the value of a *lab variable mapped to Clojure datastructures"
  [lab & params]
  (apply get-with-level lab 0 params))

(defn reshape-with
  "Rearranges a flat seq in column-major order using the given
  function to construct the innermost elements"
  [f x dims]
  ((fn reshape1 [off spc dims]
     (if (= 1 (count dims))
       ;; Make a single len-long seq containing every
       ;; spc'th item
       (f off spc dims)

       ;; Make sub-sequences
       (vec (map #(reshape1 (+ off (* spc %))
                            (* (first dims) spc)
                            (rest dims))
                 (range (first dims))))))
   0 1 dims))

(defn reshape
  "Rearranges a flat seq into column-major order into a nested row major
  vector"
  [x dims]
  (reshape-with (fn [off spc dims]
                  (vec (take (first dims) (take-nth spc (drop off x)))))
                x dims))

(defn reshape-str
  "Rearranges a flat string into column-major order into a nested row major
  vector"
  [x dims]
  (reshape-with (fn [off spc dims]
                  (str/join (take (first dims) (take-nth spc (drop off x)))))
                x dims))

(defmethod get-with-level :double
  get-with-level-double
  [lab level var]
  (let [raw (b/get lab var)
        dims (util/size lab level var)
        ndims (count dims)]
    (cond
     ;; a single item
     (every? #(= 1 %) dims) (first raw)

     ;; a single row
     (and (= ndims 2)
          (= (first dims) 1)) (vec raw)

     ;; otherwise, need to construct a nested row-major array.
     :else (reshape raw dims))))

(defmethod get-with-level :char
  get-with-level-char
  [lab level var]
  (let [dims (util/size lab level var)
        ndims (count dims)
        rows (first dims)]
    (cond

     ;; a plain string -- passthrough
     (and (= ndims 2)
          (= rows 1)) (b/get lab var)

     ;; otherwise, convert to column-major form then reshape ... 
     :else (reshape-str
            (util/get-var-part-basic-val lab
                                         level
                                         var
                                         [all])
            dims))))

;; (defn set
;;   "Sets the value of a *lab variable using a reverse of the mapping used by `get`"
;;   [lab variable value]
;;   nil)
