(ns word-families.test-helpers
  (:require-macros [word-families.macros :as m])
  (:require
   [word-families.db :as db]))

(defn load-settings [^js page]
  (.addInitScript  page
                    ;; FIXME: Map should be a param
                   #(set! (.-settings js/window) (m/stringify {::db/groups [{::db/name "Terre"
                                                                             ::db/members ["Enterrer" "Terrien"]}
                                                                            {::db/name "Dent"
                                                                             ::db/members ["Dentiste" "Dentelle"]}
                                                                            {::db/name "Autre"
                                                                             ::db/members ["Tourteau" "Terminer"]}]}))))
