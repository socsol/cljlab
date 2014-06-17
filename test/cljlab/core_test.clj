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
         (with-state-changes [(before :facts (reset! lab (cl/open :type ?type)))]

           (fact "type is correct"
                 (@lab :type) => ?type)

           (fact "handle is a future"
                 (is (future? (@lab :handle))))

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
                          (cl/open? @lab)) => false)))))

 ?type :matlab :octave)
