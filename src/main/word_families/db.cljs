(ns word-families.db
  (:require [clojure.spec.alpha :as s]))

(def default-groups [{::name "Terre"
                      ::members ["Enterrer" "Terrien" "Terrasse" "Terrier" "Extraterrestre" "Terrain" "Atterrir"]}
                     {::name "Dent"
                      ::members ["Dentiste" "Dentelle" "Dentier" "Dentaire" "Dentition" "Édenté" "Dentifrice"]}
                     {::name "Tourner"
                      ::members ["Entourer" "Détourner" "Tournoyer" "Tour" "Autour" "Tournevis" "Tourniquet"]}
                     {::name "Cheval"
                      ::members ["Chevalin" "Cavalier" "Chevalier" "Chevaleresque"]}
                     {::name "Autre"
                      ::members ["Tourteau" "Terminer" "Chevelure" "Accident"]}])

(defn- group->answers [group]
  (let [group-name (::name group)
        members (::members group)]
    (zipmap members (repeat group-name))))

(defn- groups->answers [groups]
  (reduce
   (fn [memo group]
     (merge memo (group->answers group)))
   {}
   groups))

(defn new-game [groups]
  (let [expected-answers (groups->answers groups)]
    {::group-names (map ::name  groups)
     ::expected-answers expected-answers
     ::answers {}
     ::errors {}
     ::verified? false}))

;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [settings]
  (let [groups (or (::groups settings) default-groups)]
    {::current-game (new-game groups)}))

(def answers-map? (s/map-of string? string?))

(s/def ::name string?)
(s/def ::members (s/coll-of string?))
(s/def ::group (s/keys :req [::name ::members]))
(s/def ::groups (s/coll-of ::group))
(s/def ::group-names (s/coll-of ::name))
(s/def ::expected-answers answers-map?)
(s/def ::errors answers-map?)
(s/def ::answers answers-map?)
(s/def ::verified? boolean?)
(s/def ::current-game (s/keys :req [::group-names
                                    ::expected-answers
                                    ::answers
                                    ::errors
                                    ::verified?]))
(s/def ::schema (s/keys :req [::current-game]))

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (when (not (s/valid? ::schema db))
    (let [res (s/explain-data ::schema db)
          res-as-str (s/explain-str ::schema db)]
      (.error js/console (str "schema problem: " res-as-str))
      (.dir js/console (clj->js res)))))
