(ns word-families.settings.view
  (:require
   [re-frame.core :as rf]
   [word-families.settings.spec :as spec]
   [word-families.settings.events :as events]
   [word-families.settings.subs :as subs]))

(defn render []
  (let [groups @(rf/subscribe [::subs/groups])]
    [:div#panel-root {:data-test-id "settings-panel"}
     (map
      (fn [group]
        ^{:key (::spec/name group)} [:article.group
                                   [:h3 (::spec/name group)]
                                   [:button
                                    {:on-click #(rf/dispatch [::events/delete-group group])}
                                    "Supprimer"]
                                   ])
      groups)]))
