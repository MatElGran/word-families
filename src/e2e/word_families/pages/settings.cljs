(ns word-families.pages.settings
  (:require
   [promesa.core :as p]))

(defn get-group-elements [^js locatorizable] (.locator locatorizable ".group"))

(defn collect-group-names [^js locatorizable]
  (p/let [^js group-headers  (.locator locatorizable "h3")
          group-names (.allInnerTexts group-headers)]
    (into #{} group-names)))
