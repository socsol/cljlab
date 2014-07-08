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
  (with-state-changes [(after :facts (if-not (nil? @lab) (cl/exit @lab)))]

    (fact "`open` returns something"
          (cl/open :type ?type) => truthy)

    (fact "`exit` can be called many times without issue"
          (doto (cl/open :type ?type)
            (cl/exit)
            (cl/exit)
            (cl/exit)) =not=> (throws java.lang.Exception))

    (fact "`disconnect` can be called many times without issue"
          (doto (cl/open :type ?type)
            (cl/disconnect)
            (cl/disconnect)
            (cl/disconnect)) =not=> (throws java.lang.Exception))

    (facts "about the lab"
           (with-state-changes [(before :facts (reset! lab (cl/open :type ?type)))
                                (after :facts (cl/exit @lab))]

             (fact "type is correct"
                   (@lab :type) => ?type)

             (fact "handle is a future"
                   (future? (@lab :handle)) => truthy)

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
                           (fact "returns an array of doubles"
                                 (do
                                   (cl/eval @lab "x = [1,2,3];")
                                   (cl/get-basic @lab "x")) => #(= (type %) (Class/forName "[D")))

                           (tabular
                            (fact "returns the correct elements"
                                  (do
                                    (cl/eval @lab ?code)
                                    (-> @lab
                                        (cl/get-basic ?var)
                                        (seq))) => ?values)

                            ?code               ?var ?values
                            "a = [1,2,3,4,5];"  "a"  (list 1.0 2.0 3.0 4.0 5.0)
                            "b = 1:0.5:25;"     "b"  (range 1.0 25.5 0.5)))

                    (facts "for strings"
                           (fact "returns a string"
                                 (do
                                   (cl/eval @lab "x = 'testing 1 2 3';")
                                   (cl/get-basic @lab "x")) => string?)

                           (tabular
                            (fact "returns the correct elements"
                                  (do
                                    (cl/eval @lab ?code)
                                    (cl/get-basic @lab ?var)) => ?str)

                            ?code               ?var ?str
                            "a = 'test1';"      "a"  "test1"
                            "b = 'test2\\n';"   "b"  "test2\\n"
                            "c = 'test3\\\\n';" "c"  "test3\\\\n")))))))

 ?type :matlab :octave)
