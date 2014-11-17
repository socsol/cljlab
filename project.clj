(defproject cljlab "0.2.1"
  :description "Interface to MATLAB and GNU Octave from Clojure"
  :url "https://github.com/socsol/cljlab"
  :license {:name "Apache Licence, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :scm {:name "git" :url "https://github.com/socsol/cljlab"}
  :repositories [["javaoctave" "https://kenai.com/svn/javaoctave~maven-repository/maven2"]
                 ["matlabcontrol" "https://socsol.github.io/matlabcontrol-maven"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [matlabcontrol/matlabcontrol "4.1.0"]
                 [dk.ange/javaoctave "0.6.4"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
