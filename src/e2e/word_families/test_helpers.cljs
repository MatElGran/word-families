(ns word-families.test-helpers
  (:require-macros [word-families.macros :as m])
  (:require
   [word-families.settings.db :as settings]))

(defn load-settings [^js page]
  (.addInitScript
   page
   #(let [settings (m/stringify
                     ;; FIXME: Map should be a param
                     {::settings/groups
                      [{::settings/name "Terre"
                        ::settings/members ["Enterrer" "Terrien"]
                        ::settings/traps ["Terminer"]}
                       {::settings/name "Dent"
                        ::settings/members ["Dentiste" "Dentelle"]
                        ::settings/traps ["Accident"]}]})]
      (try
        (.setItem (.-localStorage js/window) "settings" settings)
        (catch js/Error e
          ;; FIXME: report test error
          (println e))))))

(defn load-settings-invalid-edn [^js page]
  (.addInitScript
   page
   #(let [settings "{"]
      (try
        (.setItem (.-localStorage js/window) "settings" settings)
        (catch js/Error e
          ;; FIXME: report test error
          (println e))))))

(defn load-settings-invalid-schema [^js page]
  (.addInitScript
   page
   #(try
      (let [settings (m/stringify {::settings/groups [{:name "Terre"}]})]
        (.setItem (.-localStorage js/window) "settings" settings))
      (catch js/Error e
        ;; FIXME: report test error
        (println e)))))
