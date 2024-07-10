(ns word-families.settings.view
  (:require
   [re-frame.core :as rf]
   [word-families.group :as group]
   [word-families.settings.events :as events]
   [word-families.settings.subs :as subs]))

(defn- setting-item [group]
  [:article.setting-item.stack.box.rounded
   [:header.switcher
    [:h3 (::group/name group)]
    [:button.button-warning.button-small
     {:on-click #(rf/dispatch [::events/delete-group group])}
     "Supprimer"]]])

(defn- setting-items [groups]
  [:<>
   (map
    (fn [group] ^{:key (::group/name group)} [setting-item group])
    groups)])

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
        [setting-items groups]]]]]))
