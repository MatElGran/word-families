(ns word-families.settings.view
  (:require
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [word-families.group :as group]
   [word-families.settings.events :as events]
   [word-families.components.settings.group.show :as show-group-settings]
   [word-families.components.settings.group.edit :as edit-group-settings]
   [word-families.settings.subs :as subs]))

(defn- group-settings-item [group status]
  (case @status
    "show" [show-group-settings/render
            {:on-edit #(reset! status "edit")}
            group]

    ;; TODO: Add user warning when leaving the page with edited groups
    "edit" [edit-group-settings/render
            {:on-submit (fn [updated-group]
                          (reset! status "show")
                          (rf/dispatch [::events/save-group updated-group]))
             :on-delete #(rf/dispatch [::events/delete-group group])
             :on-cancel #(reset! status "show")}
            group]))

(defn- groups-settings [groups]
  [:<>
   (doall
    (map
     (fn [group] ^{:key (::group/id group)} [group-settings-item group (reagent/atom "show")])
     groups))])

(defn render []
  (let [groups @(rf/subscribe [::subs/groups])]
    [:div#panel-root
     {:data-test-id "settings-panel"}
     [:nav
      [:a {:href "/" :dangerouslySetInnerHTML {:__html "&lsaquo; Retour à l'accueil"}}]]
     [:div.center
      [:div.stack
       [:h1 "Configuration"]
       [:section.stack
        [:h2 "Groupes"]
        [groups-settings groups]]]]]))
