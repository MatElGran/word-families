(ns word-families.view
  (:require
   [re-frame.core :as rf]
   [word-families.game.view :as game]
   [word-families.settings.view :as settings]
   [word-families.routes :as routes]
   [word-families.subs :as subs]))

(defmethod routes/panels :home-panel [] [game/render])
(defmethod routes/panels :settings-panel [] [settings/render])

(defn main-view
  []
  (let [active-panel @(rf/subscribe [::subs/active-panel])]
    (routes/panels active-panel)))
