(ns word-families.settings.core
  (:require
   [clojure.spec.alpha :as s]
   [re-frame.core :as rf]
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

(defn delete-group
  [{:keys [::spec/groups] :as settings} group]
  (let [updated-groups (remove #(= (::group/id %) (::group/id group)) groups)]
    (assoc settings ::spec/groups updated-groups)))

(defn- valid-group?
  [group]
  (if (s/valid? ::group/schema group)
    true
    ;; FIXME: display validations error
    (do
      (let [res (s/explain-data ::group/schema group)]
        (rf/console :debug "Invalid form")
        (rf/console :error "schema problem: ")
        (.dir js/console (clj->js res)))
      false)))

(defn save-group
  [settings saved-group]
  (let [saved-group-id (::group/id saved-group)
        updated-groups (map
                        (fn [group]
                          (if (= (::group/id group) saved-group-id)
                            saved-group
                            group))
                        (::spec/groups settings))]

    (if (valid-group? saved-group)
      (assoc settings ::spec/groups updated-groups)
      settings)))

(defn init [user-settings]
  (let [valid-user-settings (if (s/valid? ::spec/schema user-settings) user-settings {})
        groups (or (::spec/groups valid-user-settings) default-groups)]
    {::spec/groups groups}))

(defn new-random-game [settings]
  (game/init (::spec/groups settings)))
