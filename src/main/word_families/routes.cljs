(ns word-families.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as rf]
   [word-families.events :as events]))

(defmulti panels identity)
(defmethod panels :default [] [:div "No panel found for this route."])

(def ^:private routes
  (atom
   ["/" {"" :home
         "settings" :settings}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn- url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn- dispatch
  [route]
  (let [panel (keyword (str (name (:handler route)) "-panel"))]
    (rf/dispatch [::events/set-active-panel panel])))

(defonce ^:private history
  (pushy/pushy dispatch parse))

(defn- navigate!
  [handler]
  (pushy/set-token! history (url-for handler)))

(defn start!
  []
  (pushy/start! history))

(rf/reg-fx
 :navigate
 (fn [handler]
   (navigate! handler)))
