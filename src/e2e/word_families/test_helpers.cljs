(ns word-families.test-helpers
  (:require
   ["fs" :as fs]
   [cljs.test :as t]
   [clojure.string :as str]
   [promesa.core :as p]
   [promesa.exec :as exec]
   [word-families.settings.spec :as settings]))

(defn render-init-script [local-settings]
  (let [template  (.toString (.readFileSync fs "resources/e2e/e2e_init_script.template.js"))
        settings (if (string? local-settings)
                   local-settings
                   (pr-str local-settings))
        data (str/replace template "$PLACEHOLDER" settings)
        output-path "public/js/e2e_init_script.js"]
    (.writeFileSync fs output-path data)
    output-path))

(defn load-settings
  ([^js page local-settings]
   (let [script-path (render-init-script local-settings)]
     (.addInitScript page #js {:path script-path}))))

(defn get-new-page
  ([^js browser]
   (p/let [^js page (.newPage browser)]
     (.addInitScript page #js {:path "resources/e2e/disable_re_frame_10x.js" })
     page))
  ([^js browser local-settings]
   (p/let [^js page (get-new-page browser)]
     (load-settings page local-settings)
     page)))

(defn reload-page [^js page]
  (p/then (.reload page)
          (constantly page)))

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
