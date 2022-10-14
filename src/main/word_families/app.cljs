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

(core/reg-event-db
 ::register-answer
 (fn-traced
  [db [_ groupable group]]
  (assoc-in db [::db/current-game ::db/answers groupable] group)))

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
 ::answers
 :<- [::current-game]
 (fn [game _]
   (::db/answers game)))

(rf/reg-sub
 ::current-game-group-names
 :<- [::current-game-groups]
 (fn [game-groups _]
   (map
    (fn [group]
      (::db/name group))
    game-groups)))

(rf/reg-sub
 ::current-game-groupables
 :<- [::current-game-groups]
 (fn [game-groups _]
   (flatten
    (map
     (fn [group] (::db/members group))
     game-groups))))

;; view

(defn radio-button [props]
  [:input (merge props {:type "radio"})])

(defn radio-group
  [name checked-value choices]
  [:fieldset
   [:legend name]
   (map
    (fn [input-value]
      ^{:key input-value} [:label
                           (radio-button {:name name
                                          :value input-value
                                          :on-change #(rf/dispatch [::register-answer name input-value])
                                          :checked (= checked-value input-value)})
                           input-value])
    choices)])

(defn main-view
  []
  (let [current-group-names @(rf/subscribe [::current-game-group-names])
        current-groupables @(rf/subscribe [::current-game-groupables])
        answers @(rf/subscribe [::answers])
        submit-disabled? (< (count answers)
                            (count current-groupables))]
    [:form
     {:on-submit #(.preventDefault %)}
     (map
      (fn [groupable]
        ^{:key groupable} [radio-group groupable (answers groupable) current-group-names])
      current-groupables)
     [:input {:disabled submit-disabled? :type "submit"}]]))

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
