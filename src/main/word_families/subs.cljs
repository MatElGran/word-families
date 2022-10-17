(ns word-families.subs
  (:require
   [re-frame.core :as rf]
   [word-families.db :as db]))

(rf/reg-sub
 ::current-game
 (fn [db]
   (::db/current-game db)))
