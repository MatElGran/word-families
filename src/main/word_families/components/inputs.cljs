(ns word-families.components.inputs
  (:require
   [clojure.string :as str]
   [reagent.core :as reagent]))

(defn radio-button [props]
  [:input (merge props {:type "radio"})])

(defn radio-group
  [label name current-value collection {:keys [text-fn value-fn on-change invalid?]}]
  [:fieldset.stack
   {:role "radiogroup"}
   [:legend label]
   (map
    (fn [item]
      (let [item-value (value-fn item)
            checked? (= current-value item-value)
            invalid-mark (if (and checked? invalid?)
                           {:aria-invalid true :aria-errormessage (str "error-message-" name)}
                           {})
            radio-options {:name name
                           :value item-value
                           :on-change #(on-change name item-value)
                           :checked checked?}]
         ;; TODO: pass these as children props
        ^{:key item-value} [:label
                            [radio-button (merge radio-options invalid-mark)]
                            (text-fn item)]))
    collection)

   (when invalid?
     [:div
      {:id (str "error-message-" name)}
      (str name " ne fait pas partie du groupe défini par " current-value)])])

(defn text-input [{:keys [:on-save :value]}]
  (let [val (reagent/atom value)
        save #(let [v (-> @val str str/trim)]
                (on-save v))]
    (fn [props]
      [:input (merge (dissoc props :on-save :value)
                     {:type "text"
                      :value @val
                      :on-change #(reset! val (-> % .-target .-value))
                      :on-blur save})])))

(defn textarea [{:keys [:on-save :value]}]
  (let [val (reagent/atom value)
        save #(let [v (-> @val str str/trim)]
                (on-save v))]
    (fn [props]
      [:textarea (merge (dissoc props :on-save :value)
                        {:value @val
                         :on-change #(reset! val (-> % .-target .-value))
                         :on-blur save})])))
