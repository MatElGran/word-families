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
 ::current-game-groups
 :<- [::current-game]
 (fn [game _]
   (::db/groups game)))

(rf/reg-sub
 ::current-game-group-names
 :<- [::current-game-groups]
 (fn [game-groups _]
   (map
    (fn [group]
      (::db/name group))
    game-groups)))

(rf/reg-sub
 ::current-game-word-list
 :<- [::current-game-groups]
 (fn [game-groups _]
   (flatten
    (map
     (fn [group] (::db/members group))
     game-groups))))

;; view
(defn group-selection-input
  [groups word]
  ;; FIXME: move key metadata up the stack
  ^{:key word} [:div.field
                [:label.label word]
                [:div.control
                 (map
                  #(vector :label.radio {:key %} [:input {:type "radio" :name word :value %}] %)
                  groups)]])

(defn main-view
  []
  (let [current-group-names @(rf/subscribe [::current-game-group-names])
        current-word-list @(rf/subscribe [::current-game-word-list])]
    [:<>
     [:form
      (conj (map (partial group-selection-input current-group-names) current-word-list)
            [:input {:disabled "disabled" :type "submit"}])]]))

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
