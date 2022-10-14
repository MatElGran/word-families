(ns word-families.db
  (:require [clojure.spec.alpha :as s]))

(def default-families [{::name "Terre"
                        ::members ["Enterrer" "Terrien" "Terrasse" "Terrier" "Extraterrestre" "Terrain" "Atterrir"]}
                       {::name "Dent"
                        ::members ["Dentiste" "Dentelle" "Dentier" "Dentaire" "Dentition" "Édenté" "Dentifrice"]}
                       {::name "Tourner"
                        ::members ["Entourer" "Détourner" "Tournoyer" "Tour" "Autour" "Tournevis" "Tourniquet"]}
                       {::name "Cheval"
                        ::members ["Chevalin" "Cavalier" "Chevalier" "Chevaleresque"]}])

(def default-anomalies ["Tourteau" "Terminer" "Chevelure" "Accident"])

(def default-game {::families default-families ::anomalies default-anomalies})

;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [settings]
  (let [new-db (merge {::current-game default-game} settings)]
    new-db))

(s/def ::name string?)
(s/def ::members (s/coll-of string?))
(s/def ::family (s/keys :req [::name ::members]))
(s/def ::families (s/coll-of ::family))
(s/def ::anomalies (s/coll-of string?))
(s/def ::current-game (s/keys :req [::families ::anomalies]))
(s/def ::schema (s/keys :req [::current-game]))

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (when (not (s/valid? ::schema db))
    (let [res (s/explain-data ::schema db)
          res-as-str (s/explain-str ::schema db)]
      (.error js/console (str "schema problem: " res-as-str))
      (.dir js/console (clj->js res)))))
