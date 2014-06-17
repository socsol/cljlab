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

(ns cljlab.core
  (:refer-clojure :exclude [eval type])
  (:import [matlabcontrol MatlabInvocationException MatlabProxyFactory MatlabProxyFactoryOptions$Builder]
           [dk.ange.octave OctaveEngineFactory]
           [dk.ange.octave.exception OctaveIOException]
           [dk.ange.octave.type OctaveString]
           [dk.ange.octave.type.matrix AbstractGenericMatrix]))

(defn type
  "This is used to determine the type of the lab (MATLAB or Octave)"
  [lab & _]
  (if lab
    (lab :type)
    :matlab))

(defmulti create-handle
  "Creates a handle to a lab instance"
  type)

(defmethod create-handle :matlab
  create-handle-matlab
  [{hidden :hidden :or {hidden true}}]
  (let [factory-options (-> (MatlabProxyFactoryOptions$Builder.)
                            (.setHidden hidden)
                            (.build))]
    (.getProxy (MatlabProxyFactory. factory-options))))

(defmethod create-handle :octave
  create-handle-octave
  [{}]
  (.getScriptEngine (OctaveEngineFactory.)))

(defn open
  "Returns a map that can be used to communicate with the *lab instance"
  [& {type :type, :as options :or {type :matlab}}]
  {:type type
   :handle (future (create-handle (or options {:type type})))
   :open true})

(defmulti disconnect
  "Disconnect from the lab without exiting, if this is possible"
  type)

(defmethod disconnect :matlab
  disconnect-matlab
  [lab]
  (.disconnect @(lab :handle)))

(defmethod disconnect :octave
  disconnect-octave
  [lab]
  (try (.close @(lab :handle))
       (catch OctaveIOException e nil)))

(defmulti exit
  "Close and exit the lab"
  type)

(defmethod exit :matlab
  disconnect-matlab
  [lab]
  (try (doto @(lab :handle)
         (.exit)
         (.disconnect))
       (catch MatlabInvocationException e nil)))

;; exit and disconnect are the same for Octave
(defmethod exit :octave
  disconnect-octave
  [lab]
  (disconnect lab))

(defmulti eval
  "Evaluates some *lab code; returns nothing"
  type)

(defmethod eval :matlab
  eval-matlab
  [lab code]
  (.eval @(lab :handle) code))

(defmethod eval :octave
  eval-octave
  [lab code]
  (.eval @(lab :handle) code))

(defmulti open?
  "Returns true if this lab is still in operation"
  type)

(defmethod open? :matlab
  open?-matlab
  [lab]
  (.isConnected @(lab :handle)))

(defmethod open? :octave
  open?-octave
  [lab]
  (try (do
         (eval lab "1;")
         true)
       (catch OctaveIOException e false)
       (catch java.util.concurrent.RejectedExecutionException e false)))

(defmulti get-basic
  "Returns a single variable. Can only handle flat arrays and strings"
  type)

(defmethod get-basic :matlab
  get-basic-matlab
  [lab variable]
  (.getVariable @(lab :handle) variable))

(defmethod get-basic :octave
  get-basic-octave
  [lab variable]
  (let [octave-obj (.get @(lab :handle) variable)]
       (cond
        (instance? AbstractGenericMatrix octave-obj) (.getData octave-obj)
        (instance? OctaveString octave-obj) (.getString octave-obj)
        :else nil)))

(defmulti set-basic
  "Sets a single variable.  Can only handle flat arrays and strings"
  type)

(defmethod set-basic :matlab
  set-basic-matlab
  [lab variable value]
  (.setVariable @(lab :handle) variable
                (if (seq? value)
                  (to-array value)
                  value)))

