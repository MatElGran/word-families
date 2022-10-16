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

(core/reg-event-db
 ::validate-answers
 (fn-traced
  [db _]
  (let [actual-answers (get-in db [::db/current-game ::db/answers])
        expected-answers (get-in db [::db/current-game ::db/expected-answers])
        error? (fn [answer] (not (= (get answer 1) (get expected-answers (get answer 0)))))
        errors (into {} (filter error? actual-answers))]
    (-> db
        (assoc-in [::db/current-game ::db/verified?] true)
        (assoc-in [::db/current-game ::db/errors] errors)))))
;; subs

(rf/reg-sub
 ::current-game
 (fn [db]
   (::db/current-game db)))

(rf/reg-sub
 ::expected-answers
 :<- [::current-game]
 (fn [game _]
   (::db/expected-answers game)))

(rf/reg-sub
 ::answers
 :<- [::current-game]
 (fn [game _]
   (::db/answers game)))

(rf/reg-sub
 ::errors
 :<- [::current-game]
 (fn [game _]
   (::db/errors game)))

(rf/reg-sub
 ::verified?
 :<- [::current-game]
 (fn [game _]
   (::db/verified? game)))

(rf/reg-sub
 ::valid?
 :<- [::errors]
 (fn [errors _]
   (empty? errors)))

(rf/reg-sub
 ::current-game-group-names
 :<- [::current-game]
 (fn [game _]
   (::db/group-names game)))

(rf/reg-sub
 ::current-game-groupables
 :<- [::expected-answers]
 (fn [expected-answers _]
   (keys expected-answers)))

;; view

(defn radio-button [props]
  [:input (merge props {:type "radio"})])

(defn invalid-radio-group
  [name checked-value choices]
  [:fieldset
   {:role "radiogroup"}
   [:legend name]
   (map
    (fn [input-value]
      (let [checked? (= checked-value input-value)
            invalid-mark (if checked? {:aria-invalid true :aria-errormessage (str "error-message-" name)} {})]
        ^{:key input-value} [:label
                             [radio-button
                              (merge invalid-mark {:name name
                                                   :value input-value
                                                   :on-change #(rf/dispatch [::register-answer name input-value])
                                                   :checked checked?})]
                             input-value]))
    choices)
   [:div {:id (str "error-message-" name)} (str name " ne fait pas partie du groupe défini par " checked-value)]])

(defn radio-group
  [name checked-value choices]
  [:fieldset
   {:role "radiogroup"}
   [:legend name]
   (map
    (fn [input-value]
      ^{:key input-value} [:label
                           [radio-button {:name name
                                          :value input-value
                                          :on-change #(rf/dispatch [::register-answer name input-value])
                                          :checked (= checked-value input-value)}]
                           input-value])
    choices)])

(defn validation-message []
  (let [verified? @(rf/subscribe [::verified?])
        valid? @(rf/subscribe [::valid?])]
    (when verified?
      [:span
       {:role "status"}
       (if valid?
         "Bravo! tu as gagné"
         "Il y a quelques erreurs, essaye de corriger")])))

(defn main-view
  []
  (let [current-group-names @(rf/subscribe [::current-game-group-names])
        current-groupables @(rf/subscribe [::current-game-groupables])
        answers @(rf/subscribe [::answers])
        errors @(rf/subscribe [::errors])
        submit-disabled? (< (count answers)
                            (count current-groupables))]
    [:<>
     [validation-message]
     [:form
      {:on-submit (fn [event]
                    (.preventDefault event)
                    (rf/dispatch [::validate-answers]))}
      (map
       (fn [groupable]
         (if (errors groupable)
           ^{:key groupable} [invalid-radio-group groupable (answers groupable) current-group-names]
           ^{:key groupable} [radio-group groupable (answers groupable) current-group-names]))
       current-groupables)
      [:input {:disabled submit-disabled? :type "submit"}]]]))

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
