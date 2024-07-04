(ns word-families.subs
  (:require
   [re-frame.core :as rf]
   [word-families.spec :as spec]))

(rf/reg-sub
 ::current-game
 (fn [db]
   (::spec/current-game db)))

(rf/reg-sub
 ::settings
 (fn [db]
   (::spec/settings db)))

(rf/reg-sub
 ::active-panel
 :<- [::route]
 (fn [route]
   (if (nil? (:handler route))
     :default-panel
     (keyword (str (name (:handler route)) "-panel")))))

(rf/reg-sub
 ::route
 (fn [db _]
   (::spec/route db)))
