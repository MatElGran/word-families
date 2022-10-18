(ns word-families.game.db
  (:require
   [clojure.spec.alpha :as s]))

(def answers-map? (s/map-of string? string?))

(s/def ::group-names (s/coll-of string?))
(s/def ::expected-answers answers-map?)
(s/def ::errors answers-map?)
(s/def ::answers answers-map?)
(s/def ::verified? boolean?)
(s/def ::schema (s/keys :req [::group-names
                              ::expected-answers
                              ::answers
                              ::errors
                              ::verified?]))
