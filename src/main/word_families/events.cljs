(ns word-families.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.core :as core]
   [word-families.db :as db]))

(core/reg-event-db
 ::initialize-db
 (fn-traced
  [_ [_ settings]]
  (db/initial-db settings)))
