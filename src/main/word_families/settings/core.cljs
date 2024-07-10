(ns word-families.settings.core
  (:require
   [clojure.spec.alpha :as s]
   [word-families.game.core :as game]
   [word-families.group :as group]
   [word-families.settings.spec :as spec]))

(def default-groups [(group/init "Terre"
                                 ["Enterrer" "Terrien" "Terrasse" "Terrier" "Extraterrestre" "Terrain" "Atterrir"]
                                 ["Terminer"])
                     (group/init "Dent"
                                 ["Dentiste" "Dentelle" "Dentier" "Dentaire" "Dentition" "Édenté" "Dentifrice"]
                                 ["Accident"])
                     (group/init "Tourner"
                                 ["Entourer" "Détourner" "Tournoyer" "Tour" "Autour" "Tournevis" "Tourniquet"]
                                 ["Tourteau"])
                     (group/init "Cheval"
                                 ["Chevalin" "Cavalier" "Chevalier" "Chevaleresque"]
                                 ["Chevelure"])])

(defn init [user-settings]
  (let [valid-user-settings (if (s/valid? ::spec/schema user-settings) user-settings {})
        groups (or (::spec/groups valid-user-settings) default-groups)]
    {::spec/groups groups}))

(defn- group->answers [group]
  (let [group-name (::group/name group)
        members (::group/members group)
        member-names (map ::group/name members)]
    (zipmap member-names (repeat group-name))))

(defn- groups->answers [groups]
  (reduce
   (fn [memo group]
     (merge memo (group->answers group)))
   {}
   groups))

(defn- traps-virtual-group [groups]
  {::group/name "Autre" ::group/members (flatten (map ::group/traps groups))})

(defn- expected-answers [groups]
  (let [groups (conj groups (traps-virtual-group groups))]
    (groups->answers groups)))

(defn new-random-game [settings]
  (game/init (expected-answers (::spec/groups settings))))
