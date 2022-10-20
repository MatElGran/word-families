(ns word-families.settings.subs
  (:require
   [re-frame.core :as rf]
   [word-families.subs :as subs]
   [word-families.settings.spec :as spec]))

(rf/reg-sub
 ::groups
 :<- [::subs/settings]
 (fn [settings]
   (::spec/groups settings)))
