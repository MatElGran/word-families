(ns word-families.game.subs
  (:require
   [re-frame.core :as rf]
   [word-families.game.spec :as spec]
   [word-families.subs :as subs]))

(rf/reg-sub
 ::expected-answers
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/expected-answers game)))

(rf/reg-sub
 ::answers
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/answers game)))

(rf/reg-sub
 ::errors
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/errors game)))

(rf/reg-sub
 ::verified?
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/verified? game)))

(rf/reg-sub
 ::valid?
 :<- [::errors]
 (fn [errors _]
   (empty? errors)))

(rf/reg-sub
 ::current-game-group-names
 :<- [::subs/current-game]
 (fn [game _]
   (::spec/group-names game)))

(rf/reg-sub
 ::current-game-groupables
 :<- [::expected-answers]
 (fn [expected-answers _]
   (keys expected-answers)))
