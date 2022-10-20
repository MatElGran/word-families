(ns word-families.settings.core
  (:require
   [clojure.spec.alpha :as s]
   [word-families.game.core :as game]
   [word-families.settings.spec :as spec]))

(def default-groups [{::spec/name "Terre"
                      ::spec/members ["Enterrer" "Terrien" "Terrasse" "Terrier" "Extraterrestre" "Terrain" "Atterrir"]
                      ::spec/traps ["Terminer"]}
                     {::spec/name "Dent"
                      ::spec/members ["Dentiste" "Dentelle" "Dentier" "Dentaire" "Dentition" "Édenté" "Dentifrice"]
                      ::spec/traps ["Accident"]}
                     {::spec/name "Tourner"
                      ::spec/members ["Entourer" "Détourner" "Tournoyer" "Tour" "Autour" "Tournevis" "Tourniquet"]
                      ::spec/traps ["Tourteau"]}
                     {::spec/name "Cheval"
                      ::spec/members ["Chevalin" "Cavalier" "Chevalier" "Chevaleresque"]
                      ::spec/traps ["Chevelure"]}])

(defn init [user-settings]
  (let [valid-user-settings (if (s/valid? ::spec/schema user-settings) user-settings {})
        groups (or (::spec/groups valid-user-settings) default-groups)]
    {::spec/groups groups}))

(defn- group->answers [group]
  (let [group-name (::spec/name group)
        members (::spec/members group)]
    (zipmap members (repeat group-name))))

(defn- groups->answers [groups]
  (reduce
   (fn [memo group]
     (merge memo (group->answers group)))
   {}
   groups))

(defn- traps-virtual-group [groups]
  {::spec/name "Autre" ::spec/members (flatten (map ::spec/traps groups))})

(defn- expected-answers [groups]
  (let [groups (conj groups (traps-virtual-group groups))]
    (groups->answers groups)))

(defn new-random-game [settings]
  (game/init (expected-answers (::spec/groups settings))))
