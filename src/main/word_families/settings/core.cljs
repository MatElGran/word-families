(ns word-families.settings.core
  (:require
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
