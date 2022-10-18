(ns word-families.core
  (:require
   [word-families.db :as db]
   [word-families.settings.core :as settings]))

  ;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [user-settings]
  (let [settings (settings/init user-settings)]
    ;; FIXME: should be done according to path
    {::db/active-panel :home-panel
     ::db/settings settings
     ::db/current-game (settings/new-random-game settings)}))
