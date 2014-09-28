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
  (:require [cljlab.core :as cl]
            [cljlab.basic :as b]))

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
                        (cl/eval @lab "x = $@#$@#$;") => (throws Exception))))))

 ?type :matlab :octave)

(defchecker lab-type
  "A checker to ensure the expected lab type has been returned"
  [expected]
  (checker [lab] (= (b/type lab) expected)))

;; These tests aren't very good (depends on matlab being available),
;; but stubbing `open` doesn't seem to
;; work.
(facts "about `open`"
       (fact "`open` using :auto type will use matlab if available"
             (b/open {:type :auto}) => (lab-type :matlab))

       (fact "`open` without a type will use matlab if available"
             (b/open) => (lab-type :matlab)))
