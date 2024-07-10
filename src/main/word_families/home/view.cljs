(ns word-families.home.view
  (:require
    [re-frame.core :as rf]
    [word-families.events :as events]))
(defn render []
  [:div#panel-root.cover
   {:data-test-id "home-panel"}
   [:div#main-menu.center
    [:div.box.rounded
     [:div.stack
      [:h1 "MiWoGa"]
      [:a.button.button-large
       {:href "/game" :on-click #(rf/dispatch [::events/start-new-game])}
       "Nouvelle partie"]
      [:a.button.button-large
       {:href "/settings"}
       "Configuration"]]]]])
