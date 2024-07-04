(ns word-families.core
  (:require
   [word-families.spec :as spec]
   [word-families.settings.core :as settings]))

  ;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [user-settings route]
  (let [settings (settings/init user-settings)]
    {::spec/route route
     ::spec/settings settings
     ::spec/current-game (settings/new-random-game settings)}))
