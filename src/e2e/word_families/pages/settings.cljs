(ns word-families.pages.settings
  (:require
   [promesa.core :as p]))

(defn get-group-elements [^js locatorizable] (.locator locatorizable ".setting-item"))
(defn get-delete-group-buttons [^js locatorizable] (.locator locatorizable "button" #js {:hasText "Supprimer"}))
(defn get-modify-group-buttons [^js locatorizable] (.locator locatorizable "button" #js {:hasText "Modifier"}))
(defn get-form-element [^js locatorizable] (.locator locatorizable "form"))
(defn get-submit-button [^js locatorizable] (.locator (get-form-element locatorizable) "button[type=\"submit\"]"))

(defn collect-group-names [^js locatorizable]
  (p/let [^js group-headers  (.locator locatorizable "h3")
          group-names (.allInnerTexts group-headers)]
    (into #{} group-names)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn click-modify-group-button [^js page group-name]
  (p/let [group-elements (get-group-elements page)
          group-element (.filter group-elements #js {:hasText group-name})
          modify-button (get-modify-group-buttons group-element)]
    (p/then (.click modify-button)
            (constantly page))))

(defn click-delete-group-button [^js page group-name]
  (p/let [group-elements (get-group-elements page)
          group-element (.filter group-elements #js {:hasText group-name})
          delete-button (get-delete-group-buttons group-element)]
    (p/then (.click delete-button)
            (constantly page))))

(defn click-submit-button [^js page]
  (p/let [submit-button (get-submit-button page)]
    (p/then (.click submit-button)
            (constantly page))))

;; FIXME: Scope to a group/setting item, as multiple groups can be edited at the same time
(defn fill-group-name [^js page group-name]
  (p/then (.fill (.getByLabel page "Nom du groupe") group-name)
          (constantly page)))
