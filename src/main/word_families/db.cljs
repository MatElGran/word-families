(ns word-families.db
  (:require
   [clojure.spec.alpha :as s]
   [word-families.settings.core :as settings-core]
   [word-families.game.db :as game]
   [word-families.settings.db :as settings]))

;; TODO: deserialize settings into a valid clojure structure (namespaced keys) or R/W edn ?
(defn initial-db
  [user-settings]
  (let [settings (settings-core/init user-settings)]
    {::settings settings
     ::current-game (settings-core/new-random-game settings)
     ;; FIXME: should be done according to path
     ::active-panel :home-panel}))

(s/def ::settings ::settings/schema)
(s/def ::current-game ::game/schema)
(s/def ::active-panel keyword?)
(s/def ::schema (s/keys :req [::active-panel ::current-game ::settings]))

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (when (not (s/valid? ::schema db))
    (let [res (s/explain-data ::schema db)
          res-as-str (s/explain-str ::schema db)]
      (.error js/console (str "schema problem: " res-as-str))
      (.dir js/console (clj->js res)))))
