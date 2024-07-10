(ns word-families.app
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [word-families.view :as view]
            [word-families.events :as events]
            [word-families.config :as config]
            [word-families.routes :as routes]
            [cljs.reader]))

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

(defn- debug-logger [& args]
  (when config/debug?
    (.debug js/console "Debug: " (apply str args))))

(defn init []
  (rf/set-loggers! {:debug debug-logger})
  (routes/start!)
  (let [path (-> js/window
                 .-location
                 .-pathname)
        route (routes/parse path)
        active-panel (keyword (str (name (:handler route)) "-panel"))]
    (rf/dispatch-sync [::events/initialize-db active-panel])
    (dev-setup)
    (mount-root)))
