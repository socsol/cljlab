# cljlab

[![Clojars Project](http://clojars.org/cljlab/latest-version.svg)](http://clojars.org/cljlab)

This library an interface to MATLAB and GNU Octave from within
Clojure.  This is done by leveraging two existing Java libraries,
[matlabcontrol][mc] and [JavaOctave][jo].  The point of this library
is to smooth over the differences between these two libraries so that
you can interface with whichever \*lab is available.

A lot of work still needs to be done on ferrying more complex
datatypes to/from Clojure.  At present, the following work:

 - flat arrays/lists
 - 1d strings

So the following don't work:

 - 2+d matrices (these will be flattened when pulled into clojure)
 - 2+d strings (these will error out)
 - structs
 - cells

[mc]: https://code.google.com/p/matlabcontrol/
[jo]: https://kenai.com/projects/javaoctave/pages/Home


## Additional setup

JavaOctave is available through maven, but matlabcontrol is not.  I
get around this by using [lein-localrepo][llr].  Once the plugin is
installed, you can do the following:

~~~ bash
wget https://matlabcontrol.googlecode.com/files/matlabcontrol-4.1.0.jar
lein localrepo install matlabcontrol-4.1.0.jar matlabcontrol/matlabcontrol 4.1.0
~~~

You also need to make sure that that the executables for either MATLAB
or Octave are in the path from wherever cljlab will be called.

[llr]: https://github.com/kumarshantanu/lein-localrepo


## Usage

Currently only "basic" versions of the get and set operations are
available these work only with one-dimensional character strings and
flat numeric lists.  It is intended to use these as the building
blocks for a more complete interface in the future.

~~~ clojure
(require '[cljlab :as cl])

(def my-lab (cl/open)) ;; will use either MATLAB or Octave, depending on availability
(cl/type my-lab) ;; returns :matlab or :octave

(def my-ml (cl/open {:type :matlab}))
(def my-oct (cl/open {:type :octave}))

(cl/eval my-lab "x = 1;") ;; => nil
(cl/get-basic my-lab :x) ;; => (1.0); i.e. a list of doubles.

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

Copyright 2014 Alastair Pharo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
