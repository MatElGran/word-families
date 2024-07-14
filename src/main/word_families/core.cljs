(ns word-families.core
  (:require
   [word-families.settings.core :as settings]
   [word-families.spec :as spec]))

(defn initial-db
  [user-settings route]
  (let [settings (settings/init user-settings)]
    {::spec/route route
     ::spec/settings settings
     ::spec/current-game (settings/new-random-game settings)}))
