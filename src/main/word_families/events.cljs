(ns word-families.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.lib :as lib]
   [word-families.db :as db]))

(lib/reg-event-db
 ::initialize-db
 (fn-traced
  [_ [_ settings]]
  (db/initial-db settings)))
