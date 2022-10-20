(ns word-families.test-helpers
  (:require
   ["fs" :as fs]
   [cljs.test :as t]
   [clojure.string :as str]
   [promesa.core :as p]
   [promesa.exec :as exec]
   [word-families.settings.db :as settings]))

(defn render-init-script [local-settings]
  (let [template  (.toString (.readFileSync fs "resources/e2e/e2e_init_script.template.js"))
        data (str/replace template "$PLACEHOLDER" local-settings)
        output-path "public/js/e2e_init_script.js"]
    (.writeFileSync fs output-path data)
    output-path))

(def test-default-settings {::settings/groups
                            [{::settings/name "Terre"
                              ::settings/members ["Enterrer" "Terrien"]
                              ::settings/traps ["Terminer"]}
                             {::settings/name "Dent"
                              ::settings/members ["Dentiste" "Dentelle"]
                              ::settings/traps ["Accident"]}]})

(defn load-settings
  ([^js page]
   (load-settings page (pr-str test-default-settings)))

  ([^js page local-settings]
   (let [script-path (render-init-script local-settings)]
     (.addInitScript page #js {:path script-path}))))

(defn load-settings-invalid-edn [^js page]
  (load-settings page "{"))

(defn load-settings-invalid-schema [^js page]
  (load-settings page (pr-str {::settings/groups [{:name "Terre"}]})))

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
