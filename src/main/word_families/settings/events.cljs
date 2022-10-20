(ns word-families.settings.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.lib :as lib]
   [word-families.settings.db :as db]))

(lib/reg-event-persisted-settings
 ::delete-group
 (fn-traced
  [{::db/keys [groups] :as settings} [_ group-to-delete]]
  (let [remaining-groups (filter #(not (= group-to-delete %)) groups)]
    (assoc settings ::db/groups remaining-groups))))
