(ns word-families.events
  (:require
   [cljs.reader :as reader]
   [day8.re-frame.tracing :refer [fn-traced]]
   [re-frame.core :as rf]
   [word-families.core :as core]
   [word-families.db :as db]
   [word-families.lib :as lib]))

(rf/reg-cofx
 ::local-settings
 (fn [cofx _]
   (let [local-settings (try
                          (-> js/window
                              .-localStorage
                              (.getItem "settings")
                              (reader/read-string))

                          (catch js/Errror e
                            ;; FIXME:
                            (println e)
                            false))]
     (assoc cofx :local-settings local-settings))))

(lib/reg-event-fx
 ::initialize-db
 [(rf/inject-cofx ::local-settings)]
 (fn-traced
  [cofx _]
  (let [local-settings (:local-settings cofx)]
    {:db (core/initial-db local-settings)})))

(lib/reg-event-fx
 ::navigate
 (fn-traced
  [_ [_ handler]]
  {:navigate handler}))

(lib/reg-event-db
 ::set-active-panel
 (fn-traced
  [db [_ active-panel]]
  (assoc db ::db/active-panel active-panel)))
