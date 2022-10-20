(ns word-families.lib
  (:require [re-frame.core :as rf]
            [word-families.config :as config]
            [word-families.local-storage :as local-storage]
            [word-families.spec :as spec]))

(def standard-interceptors
  [(when config/debug? rf/debug)
   (when config/debug? (rf/after spec/valid-schema?))])

(defn reg-event-db
  ([id handler-fn]
   (rf/reg-event-db id standard-interceptors handler-fn))
  ([id interceptors handler-fn]
   (rf/reg-event-db id [standard-interceptors interceptors] handler-fn)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def standard-interceptors-fx
  [(when config/debug?  rf/debug)
   (when config/debug? (rf/after #(when % (spec/valid-schema? %))))])

(defn reg-event-fx
  ([id handler-fn]
   (rf/reg-event-fx id standard-interceptors-fx handler-fn))
  ([id interceptors handler-fn]
   (rf/reg-event-fx id [standard-interceptors-fx interceptors] handler-fn)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn game-handler->db-handler
  [game-handler]
  (fn [db event]
    (let [current-game (::spec/current-game db)
          new-game (game-handler current-game event)]
      (assoc db ::spec/current-game new-game))))

(defn reg-event-game
  ([id handler-fn]
   (reg-event-game id nil handler-fn))
  ([id interceptors handler-fn]
   (reg-event-db id [interceptors] (game-handler->db-handler handler-fn))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn persisted-settings-handler->fx-handler
  [persisted-settings-handler]
  (fn
    [{:keys [db]} event]
    (let [old-settings (::spec/settings db)
          new-settings (persisted-settings-handler old-settings event)]
      {:db  (assoc db ::spec/settings new-settings)
       :fx [[::local-storage/persist-to-local-storage new-settings]]})))

(defn reg-event-persisted-settings
  ([id handler-fn]
   (reg-event-persisted-settings id nil handler-fn))
  ([id interceptors handler-fn]
   (reg-event-fx id [interceptors] (persisted-settings-handler->fx-handler handler-fn))))
