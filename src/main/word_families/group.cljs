(ns word-families.group
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(s/def ::non-blank-string (s/and string? (complement str/blank?)))

(s/def ::id uuid?)
(s/def ::name ::non-blank-string)
;; FIXME: introduce groupable ns?
(s/def ::groupable (s/keys :req [::id ::name]))
(s/def ::members (s/coll-of ::groupable))
(s/def ::traps (s/coll-of ::groupable))
(s/def ::schema (s/keys :req [::id ::name ::members ::traps]))

(defn- init-groupable
  [groupable-name]
  {::id (random-uuid) ::name groupable-name})

(defn- init-groupables
  [groupable-names]
  (map (fn [name] (init-groupable name)) groupable-names))

(defn member-names
  [group]
  (map ::name (::members group)))

(defn trap-names
  [group]
  (map ::name (::traps group)))

(defn init
  ([name members traps]
   (init (random-uuid) name members traps))
  ([id name members traps]
   {::id id
    ::name name
    ::members (init-groupables members)
    ::traps (init-groupables traps)}))
