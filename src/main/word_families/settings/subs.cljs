(ns word-families.settings.subs
  (:require
    [re-frame.core :as rf]
    [word-families.db :as db]
    [word-families.subs :as subs]))

(rf/reg-sub
  ::groups
  :<- [::subs/settings]
  (fn [settings]
    (::db/groups settings)))
