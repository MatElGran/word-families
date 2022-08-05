(ns word-families.db
  (:require [clojure.spec.alpha :as s]))

(def initial-db
  {::app-name "Word families"})

(s/def ::app-name string?)
(s/def ::schema (s/keys :req [::app-name]))

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (when (not (s/valid? ::schema db))
    (let [res (s/explain-str ::schema db)]
      (.error js/console (str "schema problem: " res)))))
