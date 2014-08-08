(defproject cljlab "0.1.0"
  :description "Interface to MATLAB and GNU Octave from Clojure"
  :url "http://example.com/FIXME"
  :license {:name "Apache Licence, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :repositories [["javaoctave" "https://kenai.com/svn/javaoctave~maven-repository/maven2"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [matlabcontrol/matlabcontrol "4.1.0"]
                 [dk.ange/javaoctave "0.6.4"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
