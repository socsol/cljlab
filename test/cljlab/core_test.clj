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
  (:require [clojure.test :refer :all]
            [cljlab.core :as cl]))

(deftest can-open
  (testing "MATLAB"
    (testing "not nil"
      (is (not (nil? (cl/open :type :matlab)))))
    (let [lab (cl/open :type :matlab)]
      (testing "is a MATLAB lab"
        (is (lab :type) :matlab)
        (is (instance? matlabcontrol.MatlabProxy @(lab :handle))))))

  (testing "Octave"
    (testing "not nil"
      (is (not (nil? (cl/open :type :octave)))))
    (let [lab (cl/open :type :octave)]
      (testing "is an Octave lab"
        (is (lab :type) :octave)
        (is (instance? dk.ange.octave.OctaveEngine @(lab :handle)))))))
