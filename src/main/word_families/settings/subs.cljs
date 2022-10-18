(ns word-families.settings.subs
  (:require
   [re-frame.core :as rf]
   [word-families.subs :as subs]
   [word-families.settings.db :as db]))

(rf/reg-sub
 ::groups
 :<- [::subs/settings]
 (fn [settings]
   (::db/groups settings)))
