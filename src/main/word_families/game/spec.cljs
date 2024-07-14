(ns word-families.game.spec
  (:require
   [clojure.spec.alpha :as s]
   [word-families.group :as group]))

(def answers-map? (s/map-of uuid? uuid?))

;; FIXME: Color must be one of the defined values
(s/def ::color string?)

(s/def ::group (s/keys :req [::group/id ::group/name ::color]))
(s/def ::groups (s/coll-of ::group))
(s/def ::expected-answers answers-map?)
(s/def ::errors answers-map?)
(s/def ::answers answers-map?)
(s/def ::selected-group-name string?)
(s/def ::display-results? boolean?)
(s/def ::verified? boolean?)

(s/def ::schema (s/keys :req [::groups
                              ::expected-answers
                              ::answers
                              ::errors
                              ::display-results?
                              ::verified?]
                        :opt [::selected-group-name]))
