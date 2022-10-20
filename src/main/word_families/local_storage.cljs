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

(rf/reg-cofx
 ::local-settings
 (fn [cofx _]
   (if local-storage
     (assoc cofx :local-settings (fetch-local-settings local-storage))
     cofx)))
