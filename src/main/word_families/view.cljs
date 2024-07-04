(ns word-families.view
  (:require
   [re-frame.core :as rf]
   [word-families.home.view :as home]
   [word-families.game.view :as game]
   [word-families.settings.view :as settings]
   [word-families.subs :as subs]))

;; FIXME: Style this
(def not-found [:div "No panel found for this route."])

(defmulti view :handler)
;; NOTE: This should not happen as there is a catch-all clause on handler selection.
(defmethod view nil [_route] not-found)
(defmethod view :not-found [_route] not-found)
(defmethod view :home [_route] [home/render])
(defmethod view :game [_route] [game/render])
(defmethod view :settings [_route] [settings/render])

(defn main-view
  []
  (let [route @(rf/subscribe [::subs/route])]
    (view route)))
