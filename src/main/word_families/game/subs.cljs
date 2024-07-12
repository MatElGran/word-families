(ns word-families.game.subs
  (:require
   [re-frame.core :as rf]
   [word-families.group :as group]
   [word-families.game.spec :as spec]
   [word-families.subs :as subs]))

(rf/reg-sub
 ::actual-answers
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/answers game)))

(rf/reg-sub
 ::errors
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/errors game)))

(rf/reg-sub
 ::display-results?
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/display-results? game)))

(rf/reg-sub
 ::valid?
 :<- [::errors]
 (fn [errors _]
   (empty? errors)))

(rf/reg-sub
 ::current-game-groups
 :<- [::subs/current-game]
 (fn [game _]
   (let [selected-group-id (::spec/selected-group-id game)]
     (map
      (fn [group]
        (assoc group :selected (= (::group/id group) selected-group-id)))
      (::spec/groups game)))))

(rf/reg-sub
 ::groups-by-id
 :<- [::current-game-groups]
 (fn
   [groups _]
   (reduce
    (fn [memo group]  (assoc memo (::group/id group) group))
    {}
    groups)))

(rf/reg-sub
 ::groupables
 :<- [::subs/current-game]
 (fn [current-game]
   (::spec/groupables current-game)))

(rf/reg-sub
 ::current-game-verified?
 :<- [::subs/current-game]
 (fn [current-game]
   (::spec/verified? current-game)))

(rf/reg-sub
 ::current-game-groupables
 :<- [::current-game-verified?]
 :<- [::groupables]
 :<- [::actual-answers]
 :<- [::groups-by-id]
 :<- [::errors]
 (fn [[current-game-verified? groupables actual-answers groups-by-id errors] _]
   (->> groupables
        (map
         (fn [groupable]
           (let [group-id (actual-answers  (::spec/id groupable))
                 group (groups-by-id group-id)
                 status (if current-game-verified?
                          (if (and errors (contains? errors (::spec/id groupable)))
                            :incorrect
                            :correct)
                          :unknown)]
             (assoc groupable
                    ::spec/answer group-id
                    ::spec/color (::spec/color group)
                    ::spec/status status)))))))

(rf/reg-sub
 ::show-form?
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/show-form? game)))

(rf/reg-sub
 ::current-game-completed?
 :<- [::current-game-groupables]
 :<- [::actual-answers]
 (fn
   [[groupables answers] _]
   (= (count answers)
      (count groupables))))
