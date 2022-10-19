(ns word-families.test-helpers
  (:require-macros [word-families.macros :as m])
  (:require
   [cljs.test :as t]
   [promesa.core :as p]
   [promesa.exec :as exec]
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

(defn after [ms fn]
  (let [promise (p/deferred)]
    (exec/schedule! ms #(p/resolve! promise))
    (p/then promise fn)))

;; Playwright `isVisible` returns immediately, which leads to flaky tests
;; So we rely on `count` which has auto-wait feature built-in
(defn assert-visible [^js locator]
  (after 20
         #(p/let [count (.count locator)]
            (t/is (> count 0)))))

(defn assert-not-visible [^js locator]
  (after 20
         #(p/let [count (.count locator)]
            (t/is (= count 0)))))
