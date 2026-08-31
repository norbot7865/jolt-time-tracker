(ns jtt.frontend.cli.compatibility-smoke
  (:require [babashka.cli :as cli]))

(def spec
  {:count {:alias :c :coerce :long :desc "Deterministic count"}
   :verbose {:alias :v :coerce :boolean :desc "Verbose flag"}})

(defn -main [& args]
  (if (some #{"--help" "-h"} args)
    (println (cli/format-opts {:spec spec :order [:count :verbose]}))
    (println
     (pr-str
      (cli/parse-opts args {:alias {:c :count :v :verbose}
                            :coerce {:count :long :verbose :boolean}
                            :restrict [:count :verbose]})))))
