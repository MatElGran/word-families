(ns word-families.components.settings.group.edit
  (:require
   [clojure.string :as str]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [word-families.components.inputs :refer [text-input textarea]]
   [word-families.group :as group]))

(defn- ->form
  [group]
  (assoc group
         ::group/members (str/join "," (group/member-names group))
         ::group/traps (str/join "," (group/trap-names group))))

(defn ->group
  [form]
  (group/init
   (::group/id form)
   (str/trim (::group/name  form))
   (remove str/blank? (map str/trim (str/split (::group/members form) ",")))
   (remove str/blank? (map str/trim (str/split (::group/traps form) ",")))))

(defn render [{:keys [on-submit on-cancel on-delete]} group]
  (let [form-data (reagent/atom (->form group))]

    (rf/console :debug "group-form values atom " form-data)

    (fn [_group] {:keys [on-submit on-cancel on-delete]}
      [:article.stack.box.rounded {:class "setting-item"}
       [:form.group-settings.stack
        {:on-submit (fn [event]
                      (.preventDefault event)
                      (on-submit (->group @form-data)))}

        [:label {:for "group-name"} "Nom du groupe"]
        [text-input {:id "group-name"
                     :auto-focus true
                     :value (::group/name @form-data)
                     :on-save (fn [new-value]
                                (swap! form-data (fn [data] (assoc data ::group/name new-value))))}]

        [:label {:for "group-members"} "Membres du groupe (séparés par une virgule)"]
        [textarea {:id "group-members"
                   :rows "3"
                   :value (::group/members @form-data)
                   :on-save (fn [new-value]
                              (swap! form-data (fn [data] (assoc data ::group/members new-value))))}]

        [:label {:for "group-traps"} "Pièges du groupe (séparés par une virgule)"]
        [textarea {:id "group-traps"
                   :rows "3"
                   :value (::group/traps @form-data)
                   :on-save (fn [new-value]
                              (swap! form-data (fn [data] (assoc data ::group/traps new-value))))}]

        [:footer.actions
         [:button.button-small.button-warning {:type "button" :on-click on-delete} "Supprimer"]
         [:span.actions-right
          [:button.button-small.button-outline {:type "reset" :on-click on-cancel} "Annuler"]
          [:button.button-small {:type "submit"}  "Enregistrer"]]]]])))
