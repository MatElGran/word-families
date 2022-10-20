(ns word-families.settings.spec
  (:require
   [clojure.spec.alpha :as s]))

  (s/def ::name string?)
  (s/def ::members (s/coll-of string?))
  (s/def ::traps (s/coll-of string?))
  (s/def ::group (s/keys :req [::name ::members ::traps]))
  (s/def ::groups (s/coll-of ::group))
  (s/def ::schema (s/keys :req [::groups]))
