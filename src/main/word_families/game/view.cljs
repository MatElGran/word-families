(ns word-families.game.view
  (:require
   [re-frame.core :as rf]
   [word-families.components.inputs :as inputs]
   [word-families.game.events :as events]
   [word-families.group :as group]
   [word-families.game.spec :as spec]
   [word-families.game.subs :as subs]))

(defn radio-groups
  [groupables groups {:keys [on-change]}]
  [:<>
   (map
    (fn [{::spec/keys [id name answer status]}]
      ^{:key id} [inputs/radio-group
                  name
                  id
                  answer
                  groups
                  {:text-fn ::group/name
                   :value-fn ::group/id
                   :on-change on-change
                   :invalid? (= status :incorrect)}])
    groupables)])

;; TODO: Add a post-verification mode which does not distinguish between correct
;; and incorrect answers
(defn- validation-message []
  (let [display-results? @(rf/subscribe [::subs/display-results?])
        valid? @(rf/subscribe [::subs/valid?])]
    (when display-results?
      [:div.modal-overlay
       (if valid?
         [:div.box.stack.modal
          {:role "status"}
          "Bravo! tu as gagné"
          [:button {:on-click #(rf/dispatch [::events/quit-game])} "Retour au menu"]]
         [:div.box.stack.modal
          {:role "status"}
          "Il y a quelques erreurs, essaye de corriger"
          [:button {:on-click #(rf/dispatch [::events/highlight-errors])} "Voir les erreurs"]])])))

(defn- card
  ([text]
   [card text {}])
  ([options text]
   [:div.box.rounded options text]))

(defn group-card
  [group]
  (let [group-id (::group/id group)
        group-name (::group/name group)
        group-color (::spec/color group)
        classes (if (:selected group) "selected" "")]
    [card
     {:class classes
      :on-click #(rf/dispatch [::events/select-group group-id])
      :data-color group-color}
     group-name]))

(defn- groupable-card
  [{::spec/keys [id name status color]}]
  (rf/console :debug status)
  (let [classes status]
    [card
     {:class classes
      :on-click #(when (not (= status :correct)) (rf/dispatch [::events/assign-groupable id]))
      :data-color color}
     name]))

(defn- deck [groups groupables]
  (let [game-verified? @(rf/subscribe [::subs/current-game-verified?])]
    [:<>
     [:div.group-cards
      (map
       (fn [group]
         ^{:key (::group/id group)} [group-card group])
       groups)]

     [:div.groupable-cards
      {:class (if game-verified? "verified" "")}
      (map
       (fn [groupable]
         ^{:key (::spec/id groupable)} [groupable-card groupable])
       groupables)]]))

(defn render
  []
  (let [groups @(rf/subscribe [::subs/current-game-groups])
        groupables @(rf/subscribe [::subs/current-game-groupables])
        completed? @(rf/subscribe [::subs/current-game-completed?])
        show-form? @(rf/subscribe [::subs/show-form?])]
    (rf/console :debug "rendering " (map clj->js  groupables))

    [:div#panel-root
     {:data-test-id "game-panel"}

     ;; Nav
     [:nav
      [:a {:href "" :on-click #(rf/dispatch [::events/quit-game]) :dangerouslySetInnerHTML {:__html "&lsaquo; Retour à l'accueil"}}]]

     [:div.center
      [:form.stack
       {:on-submit (fn [event]
                     (.preventDefault event)
                     (rf/dispatch [::events/validate-answers]))}

       (if (or show-form? false)
         [radio-groups groupables groups
          {:on-change (fn [name value] (rf/dispatch [::events/register-answer name value]))}]
         [deck groups groupables])
       [:button {:disabled (not completed?) :type "submit"} "Vérifier"]]]
     [validation-message]]))
