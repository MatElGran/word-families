(ns word-families.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]))

;; event handlers

(rf/reg-event-db
 ::initialize-db
 (fn
   [_ _]
   {::app-name "Word families"}))

;; subs

(rf/reg-sub
 ::app-name
 (fn [db]
   (::app-name db)))

;; view

(defn main-panel
  []
  (let [app-name (rf/subscribe [::app-name])]
    [:p @app-name]))

;; init

(defn init []
  (let [root-el (.getElementById js/document "root")]
    (rf/dispatch-sync [::initialize-db])
    (rdom/render [main-panel] root-el)))
