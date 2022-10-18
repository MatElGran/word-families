(ns word-families.test-helpers
  (:require-macros [word-families.macros :as m])
  (:require
   [word-families.settings.db :as settings]))

(defn load-settings [^js page]
  (.addInitScript  page
                    ;; FIXME: Map should be a param
                   #(set! (.-settings js/window) (m/stringify
                                                  {::settings/groups
                                                   [{::settings/name "Terre"
                                                     ::settings/members ["Enterrer" "Terrien"]
                                                     ::settings/traps ["Terminer"]}
                                                    {::settings/name "Dent"
                                                     ::settings/members ["Dentiste" "Dentelle"]
                                                     ::settings/traps ["Accident"]}]}))))
