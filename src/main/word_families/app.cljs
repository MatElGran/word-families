(ns word-families.app
  (:require
   [cljs.reader]
   [clojure.string :as str]
   [re-frame.core :as rf]
   [reagent.dom :as rdom]
   [word-families.config :as config]
   [word-families.events :as events]
   [word-families.routes :as routes]
   [word-families.view :as view]))

;; init

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root
  "The `^:dev/after-load` lifecycle hook indicates to shadow-cljs that this
  code must be run after every hot reload.
  This resets the re-frame app"
  []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "root")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [view/main-view] root-el)))

(defn- debug-logger
  "Custom logger, used when calling `re-frame.core/console` with :debug level.
  see `re-frame.core/set-loggers`"
  [& args]
  (when config/debug?
    (.debug js/console "%cDebug: " "font-weight: bolder; color: #32cd84;" (str/join " " args))))

(defn init []
  (rf/set-loggers! {:debug debug-logger})
  (routes/start!)
  (let [path (-> js/window
                 .-location
                 .-pathname)
        route (routes/parse path)]
    (rf/dispatch-sync [::events/initialize-db route])
    (dev-setup)
    (mount-root)))
