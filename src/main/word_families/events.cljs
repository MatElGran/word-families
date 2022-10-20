(ns word-families.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [re-frame.core :as rf]
   [word-families.core :as core]
   [word-families.spec :as spec]
   [word-families.lib :as lib]
   [word-families.local-storage :as local-storage]))

(lib/reg-event-fx
 ::initialize-db
 [(rf/inject-cofx ::local-storage/local-settings)]
 (fn-traced
  [cofx _]
  (let [local-settings (:local-settings cofx)]
    {:db (core/initial-db local-settings)})))

(lib/reg-event-fx
 ::navigate
 (fn-traced
  [_ [_ handler]]
  {:navigate handler}))

(lib/reg-event-db
 ::set-active-panel
 (fn-traced
  [db [_ active-panel]]
  (assoc db ::spec/active-panel active-panel)))
