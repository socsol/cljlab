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

(ns cljlab.basic
  (:refer-clojure :exclude [eval get set] :rename {type core-type})
  (:import [matlabcontrol MatlabInvocationException MatlabProxy MatlabProxyFactory MatlabProxyFactoryOptions$Builder]
           [dk.ange.octave OctaveEngine OctaveEngineFactory]
           [dk.ange.octave.exception OctaveIOException]
           [dk.ange.octave.type OctaveString OctaveDouble]
           [dk.ange.octave.type.matrix AbstractGenericMatrix]))

(defn type
  "This is used to determine the type of the lab (MATLAB or Octave)"
  [lab & _]
  (@lab :type))

(defn interface
  "This is used to retrieve the lab's Java interface"
  [lab & _]
  (@lab :interface))

(defn out
  "Returns the stream used to write the lab's stdout to, or nil"
  [lab & _]
  (@lab :out))

;; Note that this multimethod does not expect to receive a ref.
(defmulti create-interface
  "Creates a interface to a lab instance"
  #(% :type))

(defmulti disconnect
  "Disconnect from the lab without exiting, if this is possible"
  type)

(defmulti exit
  "Close and exit the lab"
  type)

(defmulti eval
  "Evaluates some *lab code; returns nothing"
  type)

(defmulti open?
  "Returns true if this lab is still in operation"
  type)

(defmulti get
  "Returns a single variable. Can only handle flat arrays and strings"
  type)

(defmulti set
  "Sets a single variable.  Can only handle flat arrays and strings"
  type)

(defn open
  "Returns a ref that can be used to communicate with the *lab instance"
  ([] (open {:type :auto}))
  ([options] (future
               (let [[type interface] (if (or (not (options :type)) (= (options :type) :auto))
                                        (try [:matlab
                                              (create-interface (merge options {:type :matlab}))]
                                             (catch Exception e1
                                               (try [:octave
                                                     (create-interface (merge options {:type :octave}))]
                                                    (catch Exception e2
                                                      (throw (ex-info "No lab could be launched"
                                                                      {:matlab e1
                                                                       :octave e2}))))))
                                        [(options :type) (create-interface options)])]
                 (merge options {:type type :interface interface})))))

(defmethod create-interface :matlab
  create-interface-matlab
  [{hidden :hidden :or {hidden true}}]
  (let [factory-options (-> (MatlabProxyFactoryOptions$Builder.)
                            (.setHidden hidden)
                            (.build))]
    (.getProxy (MatlabProxyFactory. factory-options))))

(defmethod create-interface :octave
  create-interface-octave
  [{out :out err :err :or {out nil err nil}}]
  (let [factory (OctaveEngineFactory.)
        engine (.getScriptEngine factory)]
    (if out
      (.setWriter engine out))
    (if err
      (.setErrorWriter engine err))
    engine))

(defmethod disconnect :matlab
  disconnect-matlab
  [lab]
  (.disconnect (interface lab)))

(defmethod disconnect :octave
  disconnect-octave
  [lab]
  (try (.close (interface lab))
       (catch OctaveIOException e nil)))

(defmethod exit :matlab
  disconnect-matlab
  [lab]
  (try (do (doto (interface lab)
             .exit
             .disconnect)
           nil)
       (catch MatlabInvocationException e nil)))

;; Call exit and then disconnect
(defmethod exit :octave
  disconnect-octave
  [lab]
  (try (do (doto lab
             (eval "exit;")
             (disconnect))
           nil)
       (catch OctaveIOException e nil)
       (catch java.util.concurrent.RejectedExecutionException e nil)))


(defmethod eval :matlab
  eval-matlab
  [lab code]
  (let [file (if (out lab)
               (java.io.File/createTempFile "cljlab" ".log")
               nil)]
    (try
      (if file
        (.eval (interface lab) (str "diary " (.getAbsolutePath file))))
      (.eval (interface lab) code)
      (finally (if file (do
                          ;; Diary off, if possible
                          (try (.eval (interface lab) "diary off")
                               (catch Exception e nil)

                               (finally
                                 ;; Read the contents of the diary and relay to out
                                 (with-open [rdr (clojure.java.io/reader file)]
                                   (dorun (map #(do (.write (out lab) (str % "\n"))) (line-seq rdr))))

                                 ;; Delete the temporary file
                                 (.delete file)))))))))

(defmethod eval :octave
  eval-octave
  [lab code]
  (.eval (interface lab) code))

(defmethod open? :matlab
  open?-matlab
  [lab]
  (.isConnected (interface lab)))

(defmethod open? :octave
  open?-octave
  [lab]
  (try (do
         (eval lab "1;")
         true)
       (catch OctaveIOException e false)
       (catch java.util.concurrent.RejectedExecutionException e false)))

(defmethod get :matlab
  get-matlab
  [lab variable]
  (let [value (.getVariable (interface lab) (name variable))]
    (cond
     (string? value) value
     (some #(instance? (Class/forName %) value) ["[D" "[Ljava.lang.Object;"]) (seq value)
     :else (throw (ex-info "Unknown type"
                           {:type (core-type value)})))))

(defmethod get :octave
  get-octave
  [lab variable]
  (let [octave-obj (.get (interface lab) (name variable))]
       (condp instance? octave-obj
         AbstractGenericMatrix (seq (.getData octave-obj))
         OctaveString          (.getString octave-obj)
         (throw (ex-info "Unknown type"
                         {:type (core-type octave-obj)})))))

(defn cast-doubles [f value]
  (let [make-double #(f (double-array %))]
    (condp = (core-type (first value))
      java.lang.Long      (make-double (map double value))
      java.lang.Boolean   (make-double (map #(if % 1.0 0.0) value))
      java.lang.Double    (make-double value)
      java.lang.Character (make-double (map (comp double int) value))
      nil               (make-double [])
      (throw (ex-info "Unknown type"
                      {:type (core-type (first value))})))))

(defmethod set :matlab
  set-matlab
  [lab variable value]
  (.setVariable (interface lab) (name variable)
                (cond
                 (string? value) value
                 (coll? value) (cast-doubles identity value)
                 :else (throw (ex-info "Unknown type"
                                       {:type (core-type value)})))))

(defmethod set :octave
  set-octave
  [lab variable value]
  (.put (interface lab) (name variable)
        (cond
         (string? value) (OctaveString. value)
         (coll? value) (cast-doubles #(OctaveDouble. % (int-array [1 (count %)])) value)
         :else (throw (ex-info "Unknown type"
                               {:type (core-type value)})))))
