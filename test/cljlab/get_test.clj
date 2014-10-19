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

(ns cljlab.get-test
  (:use midje.sweet)
  (:require [cljlab.core :as cl]
            [cljlab.basic :as b]
            [cljlab.util :as util]))

(facts "about `get`"
       (facts "for strings"
              (fact "it returns flat strings as they are"
                    (cl/get :my-lab :x) => "abcdefg"
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "char")
                     (util/size :my-lab 0 :x) => [1 8]
                     (b/get :my-lab :x) => "abcdefg"))

              (fact "it returns 2D strings as a list of strings"
                    (cl/get :my-lab :x) => ["abcd" "efgh" "ijkl"]
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "char")
                     (util/size :my-lab 0 :x) => [3 4]
                     (util/get-var-part-basic-val :my-lab 0 :x [cl/all]) => "aeibfjcgkdhl"))

              (fact "it returns higher-dimensional strings as a nested array with the last dimension concatenated"
                    (cl/get :my-lab :x) => [["abcd" "efgh"] ["ijkl" "mnop"]]
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "char")
                     (util/size :my-lab 0 :x) => [2 2 4]
                     (util/get-var-part-basic-val :my-lab 0 :x [cl/all]) => "aiembjfnckgodlhp"))))

              ;; (tabular
              ;;        (do
              ;;          (cl/eval @lab ?code)
              ;;          (cl/get @lab ?var)) => ?values)
              ;;  ?code  ?var ?values
              ;;  "[x,y,z] = meshgrid([1,2,3], [1,2,3], [1,2,3]);

                                
