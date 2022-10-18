(ns word-families.settings.core
  (:require
   [word-families.game.core :as game]
   [word-families.settings.db :as db]))

(def default-groups [{::db/name "Terre"
                      ::db/members ["Enterrer" "Terrien" "Terrasse" "Terrier" "Extraterrestre" "Terrain" "Atterrir"]}
                     {::db/name "Dent"
                      ::db/members ["Dentiste" "Dentelle" "Dentier" "Dentaire" "Dentition" "Édenté" "Dentifrice"]}
                     {::db/name "Tourner"
                      ::db/members ["Entourer" "Détourner" "Tournoyer" "Tour" "Autour" "Tournevis" "Tourniquet"]}
                     {::db/name "Cheval"
                      ::db/members ["Chevalin" "Cavalier" "Chevalier" "Chevaleresque"]}
                     {::db/name "Autre"
                      ::db/members ["Terminer" "Accident" "Tourteau" "Chevelure"]}])

(defn init [user-settings]
  (let [groups (or (::db/groups user-settings) default-groups)]
    {::db/groups groups}))

(defn- group->answers [group]
  (let [group-name (::db/name group)
        members (::db/members group)]
    (zipmap members (repeat group-name))))

(defn- groups->answers [groups]
  (reduce
   (fn [memo group]
     (merge memo (group->answers group)))
   {}
   groups))

(defn- expected-answers [groups]
  (groups->answers groups))

(defn new-random-game [settings]
  (game/init (expected-answers (::db/groups settings))))
