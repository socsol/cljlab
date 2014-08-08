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

(ns cljlab.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [cljlab.core :as cl]))

(def lab (atom nil))

(tabular
 (facts
  (fact "`open` using specified type returns something"
        (against-background (after :facts (do (if-not (nil? @lab) (cl/exit @lab)))))
        (reset! lab (cl/open {:type ?type})) => truthy)

  (fact "`exit` can be called many times without issue"
        (against-background (after :facts (do (if-not (nil? @lab) (cl/exit @lab)))))
        (doto (reset! lab (cl/open {:type ?type}))
          (cl/exit)
          (cl/exit)
          (cl/exit)) =not=> (throws Exception))

  (fact "`disconnect` can be called many times without issue"
        (against-background (after :facts (do (if-not (nil? @lab) (cl/exit @lab)))))
        (doto (reset! lab (cl/open {:type ?type}))
          (cl/disconnect)
          (cl/disconnect)
          (cl/disconnect)) =not=> (throws Exception))

  (facts "about the lab"
         (with-state-changes [(before :facts (do (if-not (nil? @lab) (cl/exit @lab))
                                                 (reset! lab (cl/open {:type ?type}))))
                              (after :facts (do (cl/exit @lab)
                                                (System/gc)))]

           (fact "`type` does not throw an exception"
                 (cl/type @lab) =not=> (throws Exception))

           (fact "`type` is correct"
                 (cl/type @lab) => ?type)

           (fact "the lab is a future"
                 (future? @lab) => truthy)

           (fact "`interface` is truthy"
                 (cl/interface @lab) => truthy)

           (facts "about `open?`"
                  (fact "initially true"
                        (cl/open? @lab) => true)

                  (fact "false after exit"
                        (do
                          (cl/exit @lab)
                          (cl/open? @lab)) => false)

                  (fact "false after disconnect"
                        (do
                          (cl/disconnect @lab)
                          (cl/open? @lab)) => false))

           (facts "about `eval`"
                  (fact "returns nil on success"
                        (cl/eval @lab "x = 1;") => nil
                        (cl/eval @lab "y = x^2;") => nil)

                  (fact "throws an exception if the input is invalid"
                        (cl/eval @lab "x = $@#$@#$;") => (throws Exception)))

           (facts "about `get-basic`"
                  (facts "for numeric arrays"
                         (fact "returns a seq"
                               (do
                                 (cl/eval @lab "x = [1,2,3];")
                                 (cl/get-basic @lab :x)) => seq?)

                         (tabular
                          (fact "returns the correct elements"
                                (do
                                  (cl/eval @lab ?code)
                                  (cl/get-basic @lab ?var)) => ?values)

                          ?code                      ?var ?values
                          "a = [1,2,3,4,5];"         :a   (list 1.0 2.0 3.0 4.0 5.0)
                          "b = linspace(1, 25, 49);" :b   (range 1.0 25.5 0.5)))

                  (facts "for strings"
                         (fact "returns a string"
                               (do
                                 (cl/eval @lab "x = 'testing 1 2 3';")
                                 (cl/get-basic @lab :x)) => string?)

                         (tabular
                          (fact "returns the correct elements"
                                (do
                                  (cl/eval @lab ?code)
                                  (cl/get-basic @lab ?var)) => ?str)
                          ?code                                ?var ?str
                          "a = 'test1';"                       :a   "test1"
                          "b = 'testing 1 2 3';"               :b   "testing 1 2 3"
                          "c = ['test3', char(10)];"           :c   "test3\n"
                          "d = ['test4', char(10), char(10)];" :d   "test4\n\n")))

           (facts "about `set-basic`"
                  (facts "for strings"
                         (tabular
                          (fact "string can be retrieved again using `get-basic`"
                                (do
                                  (cl/set-basic @lab ?var ?value)
                                  (cl/get-basic @lab ?var)) => ?value)

                          ?var       ?value
                          :x         "Testing 1 2 3"
                          :variable2 "This is a test!!!!!"
                          :some_var  "!@$#@$%#$%!#%$#!$@#$"
                          :escape    "\n\t\f\n"))

                  (facts "for numeric lists"
                         (tabular
                          (fact "list can be retrieved again using `get-basic`, but values are in double format"
                                (do
                                  (cl/set-basic @lab ?var ?value)
                                  (cl/get-basic @lab ?var)) => (map double ?value))

                          ?var       ?value
                          :integers  (list 1 2 3 4 5 6 7 8 9 10)
                          :doubles22 (range 0.5 25.5 0.5)))

                  (facts "for boolean lists"
                         (tabular
                          (fact "will be sent to lab as doubles"
                                (do
                                  (cl/set-basic @lab ?var ?value)
                                  (cl/get-basic @lab ?var)) => (map (comp double #(if % 1.0 0.0)) ?value))

                          ?var ?value
                          :x   [true]
                          :y   [false]
                          :b   (map #(= 0.0 (mod % 1)) (range 0.5 25.0 0.5))))))))

 ?type :matlab :octave)

(defchecker map-with
  "A checker to determine whether a map has a particular key-value pair"
  [key val]
  (checker [set]
           (and (contains? set key)
                (= (set key) val))))

(defchecker lab-type
  "A checker to ensure the expected lab type has been returned"
  [expected]
  (checker [lab] (= (cl/type lab) expected)))

(facts
 (fact "`open` using :auto type will use matlab if available"
       (cl/open {:type :auto}) => (lab-type :matlab)
       (provided (cl/create-interface (map-with :type :matlab)) => :matlab))

 (fact "`open` without a type will use matlab if available"
       (cl/open) => (lab-type :matlab)
       (provided (cl/create-interface (map-with :type :matlab)) => :matlab))

 (fact "`open` using :auto type will use octave if it is available and matlab is not"
       (cl/open {:type :auto}) => (lab-type :octave)
       (provided
        (cl/create-interface (map-with :type :matlab)) =throws=> (Exception. "No MATLAB allowed")
        (cl/create-interface (map-with :type :octave)) => :octave))

 (fact "`open` without type will use octave if it is available and matlab is not"
       (cl/open) => #(= (cl/type %) :octave)
       (provided
        (cl/create-interface (map-with :type :matlab)) =throws=> (Exception. "No MATLAB allowed")
        (cl/create-interface (map-with :type :octave)) => :octave)))
