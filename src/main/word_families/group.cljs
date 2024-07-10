(ns word-families.group
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(s/def ::non-blank-string (s/and string? (complement str/blank?)))

(s/def ::id uuid?)
(s/def ::name ::non-blank-string)
(s/def ::groupable (s/keys :req [::id ::name]))
(s/def ::members (s/coll-of ::groupable))
(s/def ::traps (s/coll-of ::groupable))
(s/def ::schema (s/keys :req [::id ::name ::members ::traps]))

;; FIXME: cofx
(defn- generate-id []
  (random-uuid))

(defn- init-groupable
  [groupable-name]
  {::id (generate-id) ::name groupable-name})

(defn- init-groupables
  [groupable-names]
  (map init-groupable groupable-names))

(defn init
  [name members traps]
  {::id (generate-id)
   ::name name
   ::members (init-groupables members)
   ::traps (init-groupables traps)})
