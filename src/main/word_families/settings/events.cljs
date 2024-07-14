(ns word-families.settings.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.lib :as lib]
   [word-families.settings.core :as settings]))

(lib/reg-event-persisted-settings
 ::delete-group
 (fn-traced
  [settings [_ group-to-delete]]
  (settings/delete-group settings group-to-delete)))

(lib/reg-event-persisted-settings
 ::save-group
 (fn-traced
  [settings [_ group]]
  (settings/save-group settings group)))
