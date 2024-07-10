(ns word-families.settings.spec
  (:require
   [clojure.spec.alpha :as s]
   [word-families.group :as group]))

(s/def ::groups (s/coll-of ::group/schema))
(s/def ::schema (s/keys :req [::groups]))
