(ns word-families.local-storage
  (:require
   [cljs.reader :as reader]
   [re-frame.core :as rf]))

(def local-storage
  (try
    (.-localStorage js/window)
    (catch js/Error _
      nil)))

(defn fetch-local-settings [storage]
  (try
    (-> storage
        (.getItem "settings")
        (reader/read-string))
    (catch js/Error _
      {})))

(defn save-local-settings [storage settings]
  (try
    (.setItem storage "settings" (pr-str settings))
    true
    (catch js/Error _
      false)))

(rf/reg-cofx
 ::local-settings
 (fn [cofx _]
   (if local-storage
     (assoc cofx :local-settings (fetch-local-settings local-storage))
     cofx)))

(rf/reg-fx
 ::persist-to-local-storage
 (fn
   [settings]
   (when local-storage
     (save-local-settings local-storage settings))))
