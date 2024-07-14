(ns word-families.components.settings.group.show
  (:require
   [clojure.string :as str]
   [word-families.group :as group]))

(defn render [{:keys [on-edit]} group]
  (let [member-names (group/member-names group)
        trap-names (group/trap-names group)]

    [:article.stack.box.rounded
     {:class "setting-item"}
     [:header
      [:h3 (::group/name group)]]

     [:h4 "Membres"]
     [:p (str/join ", " member-names)]
     [:h4 "Pièges"]
     [:p (str/join ", " trap-names)]

     [:footer.actions
      [:span.actions-right
       [:button.button-small
        {:on-click on-edit}
        "Modifier"]]]]))
