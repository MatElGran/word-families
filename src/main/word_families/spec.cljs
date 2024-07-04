(ns word-families.spec
  (:require
   [clojure.spec.alpha :as s]
   [re-frame.core :as rf]
   [word-families.game.spec :as game]
   [word-families.settings.spec :as settings]))

(s/def ::settings ::settings/schema)
(s/def ::current-game ::game/schema)
;; FIXME: define route spec and add it to required schema keys
(s/def ::schema (s/keys :req [::settings]
                        :opt [::current-game]))

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (when (not (s/valid? ::schema db))
    (let [res (s/explain-data ::schema db)]
      (rf/console :error "schema problem: ")
      (.dir js/console (clj->js res)))))
