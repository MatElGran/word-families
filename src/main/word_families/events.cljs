(ns word-families.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.core :as core]
   [word-families.lib :as lib]
   [word-families.db :as db]))

(lib/reg-event-db
 ::initialize-db
 (fn-traced
  [_ [_ settings]]
  (core/initial-db settings)))

(lib/reg-event-fx
 ::navigate
 (fn-traced
  [_ [_ handler]]
  {:navigate handler}))

(lib/reg-event-db
 ::set-active-panel
 (fn-traced
  [db [_ active-panel]]
  (assoc db ::db/active-panel active-panel)))
