(ns word-families.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [re-frame.core :as rf]
   [word-families.events :as events]))

(def ^:private routes
  (atom
   ["/" {"" :home
         "game" :game
         "settings" :settings
         true :not-found}]))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn- url-for
  [& args]
  (apply bidi/path-for (into [@routes] args)))

(defn- dispatch
  [route]
  (rf/console :debug "Dispatching" route)
  (rf/dispatch [::events/visit route]))

(defonce ^:private history
  (pushy/pushy dispatch parse))

(defn- navigate!
  [handler]
  (rf/console :debug "Navigating to" handler)
  (pushy/set-token! history (url-for handler)))

(defn start!
  []
  (pushy/start! history))

(rf/reg-fx
 :navigate
 (fn [handler]
   (navigate! handler)))
