(ns word-families.settings.view
  (:require
   [re-frame.core :as rf]
   [word-families.group :as group]
   [word-families.settings.events :as events]
   [word-families.components.settings.group.show :as show-group-settings]
   [word-families.settings.subs :as subs]))

(defn- groups-settings [groups]
  [:<>
   (doall
    (map
     (fn [group]
       ^{:key (::group/id group)}
       [show-group-settings/render group
        {:on-delete #(rf/dispatch [::events/delete-group group])}])
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
