# cljlab

[![Clojars Project](http://clojars.org/cljlab/latest-version.svg)](http://clojars.org/cljlab)

This library an interface to MATLAB and GNU Octave from within
Clojure.  This is done by leveraging two existing Java libraries,
[matlabcontrol][mc] and [JavaOctave][jo].  The point of this library
is to smooth over the differences between these two libraries so that
you can interface with whichever \*lab is available.

A lot of work still needs to be done on ferrying more complex
datatypes to/from Clojure.  At present, the following work:

 - getting and setting flat arrays/lists
 - getting and setting 1d strings
 - getting matrices of doubles
 - getting matrices of strings

So the following don't work:

 - setting 2+d matrices
 - setting 2+d strings
 - structs
 - cells

[mc]: https://code.google.com/p/matlabcontrol/
[jo]: https://kenai.com/projects/javaoctave/pages/Home


## Usage

There are two "levels" to the API.  The "basic" level is more or less
a straight passthrough to the underlying Java libraries, and as such
supports only a limited set of operations (`get-basic` and
`set-basic`, which operate on flat numeric and string data structures,
and `eval`).  Over top of this layer, a set of more sophisticated
"higher level" functions is being developed.  This layer is incomplete
however.

Long-term, the "higher-level" API is expected to grow to include `get`
and `set` operations on all the core datastructures, as well as a
means of easily wrapping functions.

~~~ clojure
(require '[cljlab :as cl])

(def my-lab (cl/open)) ;; will use either MATLAB or Octave, depending on availability
(cl/type my-lab) ;; returns :matlab or :octave

(def my-ml (cl/open {:type :matlab}))
(def my-oct (cl/open {:type :octave}))

;; "Basic" get
(cl/eval my-lab "x = 1;") ;; => nil
(cl/get-basic my-lab :x) ;; => (1.0); i.e. a list of doubles.
(cl/eval my-lab "x = reshape(1:27, [3 3 3]);") ;; => nil
(cl/get-basic my-lab :x) ;; => (range 1.0 28.0); i.e. flattened

;; "Higher-level" get
(cl/get my-lab :x) ;; =>
                   ;; [[[1.0 10.0 19.0] [4.0 13.0 22.0] [7.0 16.0 25.0]]
                   ;;  [[2.0 11.0 20.0] [5.0 14.0 23.0] [8.0 17.0 26.0]]
                   ;;  [[3.0 12.0 21.0] [6.0 15.0 24.0] [9.0 18.0 27.0]]]

;; "Basic" set
(cl/set-basic my-lab :y (range 0 100 0.5)) ;; => nil; puts a list of doubles into the lab
(cl/get-basic my-lab :y) ;; => (range 0 100 0.5)

(cl/set-basic my-lab :some_str "Testing 1 2 3\n") ;; puts a flat string into some_str in the lab
(cl/eval my-lab "fprintf(some_str);" ;; Won't see anything as stdin/out are not connected up

(cl/open? my-lab) ;; => true

(cl/exit my-lab) ;; Kills the lab

(cl/open? my-lab) ;; => false

(cl/disconnect my-ml) ;; Should leave the lab running after Java exits
(cl/open? my-ml) ;; => false
~~~


## License

`cljlab` is licensed under [the Apache License, Version 2.0][lic].

[lic]: http://www.apache.org/licenses/LICENSE-2.0
