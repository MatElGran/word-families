(ns word-families.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [word-families.core :as core]
            [word-families.config :as config]
            [word-families.db :as db]
            [cljs.reader]))

;; event handlers

(core/reg-event-db
 ::initialize-db
 (fn-traced
  [_ [_ settings]]
  (db/initial-db settings)))

;; subs

(rf/reg-sub
 ::current-game
 (fn [db]
   (::db/current-game db)))

(rf/reg-sub
 ::current-game-families
 :<- [::current-game]
 (fn [game _]
   (::db/groups game)))

(rf/reg-sub
 ::current-game-anomalies
 :<- [::current-game]
 (fn [game _]
   (::db/anomalies game)))

(rf/reg-sub
 ::current-game-family-names
 :<- [::current-game-families]
 (fn [game-families _]
   (map
    (fn [family]
      (::db/name family))
    game-families)))

(rf/reg-sub
 ::current-game-family-words
 :<- [::current-game-families]
 (fn [game-families _]
   (flatten
    (map
     (fn [family] (::db/members family))
     game-families))))

(rf/reg-sub
 ::current-game-word-list
 :<- [::current-game-family-words]
 :<- [::current-game-anomalies]
 (fn [[family-words anomalies] _]
   (let [all-words (apply conj family-words anomalies)]
     (shuffle all-words))))

;; view
(defn family-selection-input
  [families word]
  ;; FIXME: move key metadata up the stack
  ^{:key word} [:div.field
                [:label.label word]
                [:div.control
                 (map
                  #(vector :label.radio {:key %} [:input {:type "radio" :name word :value %}] %)
                  families)]])

(defn main-view
  []
  (let [current-family-names @(rf/subscribe [::current-game-family-names])
        current-word-list @(rf/subscribe [::current-game-word-list])]
    (println current-family-names)
    [:<>
     [:form
      (conj (map (partial family-selection-input (conj current-family-names "Autre" )) current-word-list) [:input {:disabled "disabled" :type "submit"}])]]))


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
  (rf/dispatch-sync [::initialize-db (cljs.reader/read-string (.-settings js/window))])
  (dev-setup)
  (mount-root))
