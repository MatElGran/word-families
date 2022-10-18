(ns word-families.settings.view
  (:require
   [re-frame.core :as rf]
   [word-families.db :as db]
   [word-families.settings.subs :as subs]))

(defn render []
  (let [groups @(rf/subscribe [::subs/groups])]
    [:div#panel-root {:data-test-id "settings-panel"}
     (map (fn [group] ^{:key (::db/name group)} [:article.group [:h3 (::db/name group)]]) groups)]))
