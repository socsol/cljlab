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

(ns cljlab.util-test
  (:use midje.sweet)
  (:require [cljlab.basic :as b]
            [cljlab.util :as util]))

(fact "`clear` executes `clear` commands on the lab"
      (util/clear :my-lab :a :b :c) => (list :ok_a :ok_b :ok_c)
      (provided
       (b/eval :my-lab "clear a") => :ok_a
       (b/eval :my-lab "clear b") => :ok_b
       (b/eval :my-lab "clear c") => :ok_c))

(facts "about `with-placeholders`"
       (fact "it evaluates each form and returns the last one"
             (util/with-placeholders :my-lab []
               (println "test1")
               (println "test2")) => :ok
               (provided
                (println "test1") => nil
                (println "test2") => :ok))

       (fact "it calls `clear` on the list of placeholders after evaluating"
             (util/with-placeholders :my-lab [:pl1 :pl2]
               :ok) => :ok
               (provided
                (util/clear :my-lab :pl1 :pl2) => nil)))

(fact "`call-basic` does a set, eval, get loop"
      (util/call-basic :my-lab 1 :sum [1] [2] [3]) => '((4.0))
      (provided
       (b/set :my-lab :__cljlab_in0__ [1]) => nil
       (b/set :my-lab :__cljlab_in1__ [2]) => nil
       (b/set :my-lab :__cljlab_in2__ [3]) => nil
       (b/eval :my-lab "[__cljlab_out0__] = sum(__cljlab_in0__,__cljlab_in1__,__cljlab_in2__);") => nil
       (b/get :my-lab :__cljlab_out0__) => (list 4.0)
       (b/eval :my-lab "clear __cljlab_in0__") => nil
       (b/eval :my-lab "clear __cljlab_in1__") => nil
       (b/eval :my-lab "clear __cljlab_in2__") => nil
       (b/eval :my-lab "clear __cljlab_out0__") => nil)

      (util/call-basic :my-lab 0 :prod [1] [2]) => nil
      (provided
       (b/set :my-lab :__cljlab_in0__ [1]) => nil
       (b/set :my-lab :__cljlab_in1__ [2]) => nil
       (b/eval :my-lab "prod(__cljlab_in0__,__cljlab_in1__);") => nil
       (b/eval :my-lab "clear __cljlab_in0__") => nil
       (b/eval :my-lab "clear __cljlab_in1__") => nil))

(fact "`size` returns the size of a variable as a vector"
      (util/size :my-lab :x) => [1 2 3]
      (provided 
       (util/call-basic :my-lab 1 :size [:x]) => '((1.0 2.0 3.0))))

(fact "`class` returns the class of a variable as a keyword"
      (util/class :my-lab :z) => :double
      (provided
       (util/call-basic :my-lab 1 :class [:z]) => '("double")))
