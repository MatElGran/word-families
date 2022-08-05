(ns word-families.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [word-families.core :as core]
            [word-families.config :as config]
            [word-families.db :as db]))

;; event handlers

(core/reg-event-db
 ::initialize-db
 (fn-traced
  [_ _]
  db/initial-db))

;; subs

(rf/reg-sub
 ::app-name
 (fn [db]
   (::db/app-name db)))

;; view

(defn main-view
  []
  (let [app-name (rf/subscribe [::app-name])]
    [:p @app-name]))

;; init

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root
  "The `^:dev/after-load` lifecycle hook indicates to shadow-cljs that this
  code must be run after every hot reload.
  This resets the re-frame app"
  []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "root")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [main-view] root-el)))

(defn init []
  (rf/dispatch-sync [::initialize-db])
  (dev-setup)
  (mount-root))
