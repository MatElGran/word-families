(ns word-families.pages.settings
  (:require
   [promesa.core :as p]))

(defn get-group-elements [^js locatorizable] (.locator locatorizable ".group"))
(defn get-delete-group-buttons [^js locatorizable] (.locator locatorizable "button" #js {:hasText "Supprimer"}))

(defn collect-group-names [^js locatorizable]
  (p/let [^js group-headers  (.locator locatorizable "h3")
          group-names (.allInnerTexts group-headers)]
    (into #{} group-names)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn click-delete-group-button [^js page group-name]
  (p/let [group-elements (get-group-elements page)
          group-element (.filter group-elements #js {:hasText group-name})
          delete-button (get-delete-group-buttons group-element)]
    (p/then (.click delete-button)
            (constantly page))))
