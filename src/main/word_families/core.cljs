(ns word-families.core
  (:require
   [word-families.spec :as spec]
   [word-families.settings.core :as settings]))

  ;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [user-settings]
  (let [settings (settings/init user-settings)]
    ;; FIXME: should be done according to path
    {::spec/active-panel :home-panel
     ::spec/settings settings
     ::spec/current-game (settings/new-random-game settings)}))
