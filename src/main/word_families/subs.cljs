(ns word-families.subs
  (:require
   [re-frame.core :as rf]
   [word-families.db :as db]))

(rf/reg-sub
 ::current-game
 (fn [db]
   (::db/current-game db)))

(rf/reg-sub
 ::expected-answers
 :<- [::current-game]
 (fn [game _]
   (::db/expected-answers game)))

(rf/reg-sub
 ::answers
 :<- [::current-game]
 (fn [game _]
   (::db/answers game)))

(rf/reg-sub
 ::errors
 :<- [::current-game]
 (fn [game _]
   (::db/errors game)))

(rf/reg-sub
 ::verified?
 :<- [::current-game]
 (fn [game _]
   (::db/verified? game)))

(rf/reg-sub
 ::valid?
 :<- [::errors]
 (fn [errors _]
   (empty? errors)))

(rf/reg-sub
 ::current-game-group-names
 :<- [::current-game]
 (fn [game _]
   (::db/group-names game)))

(rf/reg-sub
 ::current-game-groupables
 :<- [::expected-answers]
 (fn [expected-answers _]
   (keys expected-answers)))
