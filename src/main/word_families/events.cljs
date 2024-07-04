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
  [cofx [_ route]]
  (let [local-settings (:local-settings cofx)]
    {:db (core/initial-db local-settings route)})))

(lib/reg-event-fx
 ::start-new-game
 (fn-traced
  [{:keys [db]}  _]
  (let [game (settings/new-random-game (::spec/settings db))]
    {:db (assoc db ::spec/current-game game)
     :navigate :game})))

(lib/reg-event-db
 ::reset-game
 (fn-traced
  [db  _]
  (dissoc db ::spec/current-game)))

(lib/reg-event-fx
 ::navigate
 (fn-traced
  [_  [_ handler]]
  {:navigate handler}))

(lib/reg-event-db
 ::visit
 (fn-traced
  [db [_ route]]
  (assoc db ::spec/route route)))
