(ns word-families.db
  (:require
   [clojure.spec.alpha :as s]
   [word-families.settings.db :as settings]))

(def default-groups [{::settings/name "Terre"
                      ::settings/members ["Enterrer" "Terrien" "Terrasse" "Terrier" "Extraterrestre" "Terrain" "Atterrir"]}
                     {::settings/name "Dent"
                      ::settings/members ["Dentiste" "Dentelle" "Dentier" "Dentaire" "Dentition" "Édenté" "Dentifrice"]}
                     {::settings/name "Tourner"
                      ::settings/members ["Entourer" "Détourner" "Tournoyer" "Tour" "Autour" "Tournevis" "Tourniquet"]}
                     {::settings/name "Cheval"
                      ::settings/members ["Chevalin" "Cavalier" "Chevalier" "Chevaleresque"]}
                     {::settings/name "Autre"
                      ::settings/members ["Tourteau" "Terminer" "Chevelure" "Accident"]}])

(defn- group->answers [group]
  (let [group-name (::settings/name group)
        members (::settings/members group)]
    (zipmap members (repeat group-name))))

(defn- groups->answers [groups]
  (reduce
   (fn [memo group]
     (merge memo (group->answers group)))
   {}
   groups))

(defn new-game [groups]
  (let [expected-answers (groups->answers groups)]
    {::group-names (map ::settings/name  groups)
     ::expected-answers expected-answers
     ::answers {}
     ::errors {}
     ::verified? false}))

;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [settings]
  (let [groups (or (::settings/groups settings) default-groups)]
    {::settings {::settings/groups groups}
     ::current-game (new-game groups)
     ;; FIXME: should be done according to path
     ::active-panel :home-panel}))

(def answers-map? (s/map-of string? string?))

(s/def ::group-names (s/coll-of ::settings/name))
(s/def ::expected-answers answers-map?)
(s/def ::errors answers-map?)
(s/def ::answers answers-map?)
(s/def ::verified? boolean?)
(s/def ::current-game (s/keys :req [::group-names
                                    ::expected-answers
                                    ::answers
                                    ::errors
                                    ::verified?]))

(s/def ::settings ::settings/schema)
(s/def ::active-panel keyword?)
(s/def ::schema (s/keys :req [::active-panel ::current-game ::settings]))

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (when (not (s/valid? ::schema db))
    (let [res (s/explain-data ::schema db)
          res-as-str (s/explain-str ::schema db)]
      (.error js/console (str "schema problem: " res-as-str))
      (.dir js/console (clj->js res)))))
