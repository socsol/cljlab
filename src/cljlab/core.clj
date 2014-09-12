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
  (:refer-clojure :exclude [eval get set type])
  (:require [clojure.string :as str]
            [cljlab.basic :as b]
            [cljlab.expr :as expr]))

;; These are imported from the "basic" namespace
(def open b/open)
(def exit b/exit)
(def disconnect b/disconnect)
(def type b/type)
(def interface b/interface)
(def open? b/open?)
(def eval b/eval)

(defn get
  "Returns the value of a *lab variable mapped to Clojure datastructures"
  [lab variable]
  nil)

(defn set
  "Sets the value of a *lab variable using a reverse of the mapping used by `get`"
  [lab variable value]
  nil)
