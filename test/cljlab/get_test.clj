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
                     (util/get-var-part-basic-val :my-lab 0 :x [cl/all]) => "aiembjfnckgodlhp")))

       (facts "for doubles"
              (fact "it returns single elements as scalars"
                    (cl/get :my-lab :x) => 1.0
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "double")
                     (util/size :my-lab 0 :x) => [1 1]
                     (b/get :my-lab :x) => (list 1.0)))


              (fact "it returns row vectors as plain vectors"
                    (cl/get :my-lab :x) => [1.0 2.0 3.0]
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "double")
                     (util/size :my-lab 0 :x) => [1 3]
                     (b/get :my-lab :x) => (list 1.0 2.0 3.0)))

              (fact "it returns column vectors as nested vectors"
                    (cl/get :my-lab :x) => [[1.0] [2.0] [3.0]]
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "double")
                     (util/size :my-lab 0 :x) => [3 1]
                     (b/get :my-lab :x) => (list 1.0 2.0 3.0)))

              (fact "it returns 2D matrices as nested vectors"
                    (cl/get :my-lab :x) => [[1.0 4.0] [2.0 5.0] [3.0 6.0]]
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "double")
                     (util/size :my-lab 0 :x) => [3 2]
                     (b/get :my-lab :x) => (list 1.0 2.0 3.0 4.0 5.0 6.0)))

              (fact "it returns 3D matrices as nested vectors"
                    (cl/get :my-lab :x) => [[[1.0 10.0 19.0] [4.0 13.0 22.0] [7.0 16.0 25.0]]
                                            [[2.0 11.0 20.0] [5.0 14.0 23.0] [8.0 17.0 26.0]]
                                            [[3.0 12.0 21.0] [6.0 15.0 24.0] [9.0 18.0 27.0]]]
                    (provided
                     (util/call-fn-with-vars :my-lab 0 1 :class :x) => (list "double")
                     (util/size :my-lab 0 :x) => [3 3 3]
                     (b/get :my-lab :x) => (range 1.0 28.0)))))

                                
