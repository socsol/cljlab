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

(ns cljlab.basic-test
  (:use midje.sweet)
  (:require [cljlab.basic :as cl]))

(def lab (atom nil))

(fact-group
 :basic-interface
 (tabular
  (facts "about the basic interface"
         (with-state-changes [(before :facts (do (if-not (nil? @lab) (cl/exit @lab))
                                                 (reset! lab (cl/open {:type ?type}))))
                              (after :facts (do (cl/exit @lab)
                                                (System/gc)))]

           (facts "about `get`"
                  (facts "for numeric arrays"
                         (fact "returns a seq"
                               (do
                                 (cl/eval @lab "x = [1,2,3];")
                                 (cl/get @lab :x)) => seq?)

                         (tabular
                          (fact "returns the correct elements"
                                (do
                                  (cl/eval @lab ?code)
                                  (cl/get @lab ?var)) => ?values)

                          ?code                      ?var ?values
                          "a = [1,2,3,4,5];"         :a   (list 1.0 2.0 3.0 4.0 5.0)
                          "b = linspace(1, 25, 49);" :b   (range 1.0 25.5 0.5)))

                  (facts "for strings"
                         (fact "returns a string"
                               (do
                                 (cl/eval @lab "x = 'testing 1 2 3';")
                                 (cl/get @lab :x)) => string?)

                         (tabular
                          (fact "returns the correct elements"
                                (do
                                  (cl/eval @lab ?code)
                                  (cl/get @lab ?var)) => ?str)
                          ?code                                ?var ?str
                          "a = 'test1';"                       :a   "test1"
                          "b = 'testing 1 2 3';"               :b   "testing 1 2 3"
                          "c = ['test3', char(10)];"           :c   "test3\n"
                          "d = ['test4', char(10), char(10)];" :d   "test4\n\n")))

           (facts "about `set`"
                  (facts "for strings"
                         (tabular
                          (fact "string can be retrieved again using `get`"
                                (do
                                  (cl/set @lab ?var ?value)
                                  (cl/get @lab ?var)) => ?value)

                          ?var       ?value
                          :x         "Testing 1 2 3"
                          :variable2 "This is a test!!!!!"
                          :some_var  "!@$#@$%#$%!#%$#!$@#$"
                          :escape    "\n\t\f\n"))

                  (facts "for numeric lists"
                         (tabular
                          (fact "list can be retrieved again using `get`, but values are in double format"
                                (do
                                  (cl/set @lab ?var ?value)
                                  (cl/get @lab ?var)) => (map double ?value))

                          ?var       ?value
                          :integers  (list 1 2 3 4 5 6 7 8 9 10)
                          :doubles22 (range 0.5 25.5 0.5)))

                  (facts "for boolean lists"
                         (tabular
                          (fact "will be sent to lab as doubles"
                                (do
                                  (cl/set @lab ?var ?value)
                                  (cl/get @lab ?var)) => (map (comp double #(if % 1.0 0.0)) ?value))

                          ?var ?value
                          :x   [true]
                          :y   [false]
                          :b   (map #(= 0.0 (mod % 1)) (range 0.5 25.0 0.5)))))))

  ?type :matlab :octave))

(fact-group
 :basic-output
 (tabular
  (facts "about output passthrough"
         (with-state-changes [(after :facts (do (cl/exit @lab) (System/gc)))]
           
           (facts "when :out is specified"
                  (fact "output produced during eval is sent to the specified OutputWriter"
                        (with-out-str
                          (do
                            (if-not (nil? @lab) (cl/exit @lab))
                            (reset! lab (cl/open {:type ?type :out *out*}))
                            (cl/eval @lab "disp('hello world');"))) => "hello world\n"))

           (facts "when :out is not specified"
                  (fact "output produced during eval is not sent to stdout"
                        (with-out-str
                          (do
                            (if-not (nil? @lab) (cl/exit @lab))
                            (reset! lab (cl/open {:type ?type}))
                            (cl/eval @lab "disp('hello world');"))) => ""))))

  ?type :matlab :octave))
