(ns word-families.lib
  (:require [re-frame.core :as rf]
            [word-families.config :as config]
            [word-families.db :as db]))

(def standard-interceptors
  [(when config/debug? rf/debug)
   (when config/debug? (rf/after db/valid-schema?))])

(def standard-interceptors-fx
  [(when config/debug?  rf/debug)
   (when config/debug? (rf/after #(when % (db/valid-schema? %))))])

(defn reg-event-db
  ([id handler-fn]
   (rf/reg-event-db id standard-interceptors handler-fn))
  ([id interceptors handler-fn]
   (rf/reg-event-db id [standard-interceptors interceptors] handler-fn)))

(defn reg-event-fx
  ([id handler-fn]
   (rf/reg-event-fx id standard-interceptors-fx handler-fn))
  ([id interceptors handler-fn]
   (rf/reg-event-fx
    id
    [standard-interceptors-fx interceptors]
    handler-fn)))
