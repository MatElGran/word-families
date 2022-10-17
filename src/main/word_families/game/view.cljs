(ns word-families.game.view
  (:require
   [re-frame.core :as rf]
   [word-families.game.events :as events]
   [word-families.subs :as subs]))

(defn- radio-button [props]
  [:input (merge props {:type "radio"})])

(defn- invalid-radio-group
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
                                                   :on-change #(rf/dispatch [::events/register-answer name input-value])
                                                   :checked checked?})]
                             input-value]))
    choices)
   [:div {:id (str "error-message-" name)} (str name " ne fait pas partie du groupe défini par " checked-value)]])

(defn- radio-group
  [name checked-value choices]
  [:fieldset
   {:role "radiogroup"}
   [:legend name]
   (map
    (fn [input-value]
      ^{:key input-value} [:label
                           [radio-button {:name name
                                          :value input-value
                                          :on-change #(rf/dispatch [::events/register-answer name input-value])
                                          :checked (= checked-value input-value)}]
                           input-value])
    choices)])

(defn- validation-message []
  (let [verified? @(rf/subscribe [::subs/verified?])
        valid? @(rf/subscribe [::subs/valid?])]
    (when verified?
      [:span
       {:role "status"}
       (if valid?
         "Bravo! tu as gagné"
         "Il y a quelques erreurs, essaye de corriger")])))

(defn render
  []
  (let [current-group-names @(rf/subscribe [::subs/current-game-group-names])
        current-groupables @(rf/subscribe [::subs/current-game-groupables])
        answers @(rf/subscribe [::subs/answers])
        errors @(rf/subscribe [::subs/errors])
        submit-disabled? (< (count answers)
                            (count current-groupables))]
    [:<>
     [validation-message]
     [:form
      {:on-submit (fn [event]
                    (.preventDefault event)
                    (rf/dispatch [::events/validate-answers]))}
      (map
       (fn [groupable]
         (if (errors groupable)
           ^{:key groupable} [invalid-radio-group groupable (answers groupable) current-group-names]
           ^{:key groupable} [radio-group groupable (answers groupable) current-group-names]))
       current-groupables)
      [:input {:disabled submit-disabled? :type "submit"}]]]))
