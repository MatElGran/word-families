(ns word-families.game.core
  (:require [word-families.game.spec :as spec]))

(defn init [expected-answers]
  (let [group-names (into #{} (vals expected-answers))]
    {::spec/group-names group-names
     ::spec/expected-answers expected-answers
     ::spec/answers {}
     ::spec/errors {}
     ::spec/verified? false}))
