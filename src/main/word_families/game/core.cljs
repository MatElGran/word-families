(ns word-families.game.core
  (:require [word-families.game.db :as db]))

(defn init [expected-answers]
  (let [group-names (into #{} (vals expected-answers))]
    {::db/group-names group-names
     ::db/expected-answers expected-answers
     ::db/answers {}
     ::db/errors {}
     ::db/verified? false}))
