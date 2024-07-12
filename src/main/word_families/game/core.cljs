(ns word-families.game.core
  (:require
   [word-families.group :as group]
   [word-families.game.spec :as spec]))

(def color-palette
  ["blue" "yellow" "purple" "green" "red"])

;; FIXME: COFX
(defn generate-id []
  (random-uuid))

(defn- group->answers [group]
  (let [members (::group/members group)]
    (zipmap (map ::group/id members) (repeat (::group/id group)))))

(defn- groups->answers [groups]
  (reduce
   (fn [memo group]
     (merge memo (group->answers group)))
   {}
   groups))

(defn- traps-virtual-group [groups]
  {::group/id (generate-id)
   ::group/name "Autre"
   ::group/members (flatten (map ::group/traps groups))})

(defn- decorate-members [group]
  (map
   (fn [groupable]
     {::spec/id (::group/id groupable)
      ::spec/name (::group/name groupable)
      ::spec/solution (::group/id group)
      ::spec/status :unknown})
   (::group/members group)))

(defn- groupables
  [groups]
  (shuffle (flatten (map decorate-members groups))))

(defn- selected-groups
  [available-groups]
  (let [selected-groups (->> available-groups
                             shuffle
                             (take 4))
        traps-virtual-group (traps-virtual-group selected-groups)
        all-groups (conj selected-groups traps-virtual-group)
        color-by-group (zipmap all-groups (shuffle color-palette))]
    (reverse
     (map
      (fn [group]
        (assoc group ::spec/color (color-by-group group)))
      all-groups))))

(defn init [available-groups]
  (let [groups (selected-groups available-groups)]
    {::spec/groups groups
     ::spec/groupables (groupables groups)
     ::spec/expected-answers (groups->answers groups)
     ::spec/selected-group-id (::group/id (first groups))
     ::spec/answers {}
     ::spec/errors {}
     ::spec/display-results? false
     ::spec/verified? false}))
