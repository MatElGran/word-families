(ns word-families.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [re-frame.core :as rf]
   [word-families.core :as core]
   [word-families.lib :as lib]
   [word-families.local-storage :as local-storage]
   [word-families.settings.core :as settings]
   [word-families.spec :as spec]))

(lib/reg-event-fx
 ::initialize-db
 [(rf/inject-cofx ::local-storage/local-settings)]
 (fn-traced
  [cofx [_ active-panel]]
  (let [local-settings (:local-settings cofx)]
    {:db (core/initial-db local-settings active-panel)})))

(lib/reg-event-fx
 ::start-new-game
 (fn-traced
  [{:keys [db]}  _]
  (let [game (settings/new-random-game (::spec/settings db))]
    {:db (assoc db ::spec/current-game game)
     :navigate :game})))

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
