(ns word-families.settings.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.lib :as lib]
   [word-families.settings.spec :as spec]))

(lib/reg-event-persisted-settings
 ::delete-group
 (fn-traced
  [{::spec/keys [groups] :as settings} [_ group-to-delete]]
  (let [remaining-groups (filter #(not (= group-to-delete %)) groups)]
    (assoc settings ::spec/groups remaining-groups))))
