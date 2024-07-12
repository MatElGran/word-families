(ns word-families.game.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.game.spec :as spec]
   [word-families.events :as events]
   [word-families.lib :as lib]))

(lib/reg-event-game
 ::select-group
 (fn-traced
  [game [_ group-id]]
  (assoc game ::spec/selected-group-id group-id)))

(lib/reg-event-game
 ::assign-groupable
 (fn-traced
  [game [_ groupable-id]]
  (if-let [group-id (::spec/selected-group-id game)]
    (let [old-answer (get-in game [::spec/answers groupable-id])]
      (if (= old-answer group-id)
        (update game ::spec/answers (fn [answers] (dissoc answers groupable-id)))
        (assoc-in game [::spec/answers groupable-id] group-id)))
    game)))

(lib/reg-event-game
 ::register-answer
 (fn-traced
  [game [_ groupable-id group-id]]
  (assoc-in game [::spec/answers groupable-id] group-id)))

(lib/reg-event-game
 ::validate-answers
 (fn-traced
  [game _]
  (let [actual-answers (::spec/answers game)
        expected-answers (::spec/expected-answers game)
        error? (fn [[groupable-id group-id]]
                 (not (= group-id (get expected-answers groupable-id))))
        errors (into {} (if (empty? actual-answers)
                          (zipmap (keys expected-answers) (repeat nil))
                          (filter error? actual-answers)))]
    (assoc game ::spec/verified? true ::spec/display-results? true ::spec/errors errors))))

(lib/reg-event-game
 ::highlight-errors
 (fn-traced
  [game _]
  (assoc game ::spec/display-results? false)))

(lib/reg-event-fx
 ::quit-game
 (fn-traced
  [_ _]
  {:fx [[:dispatch [::events/reset-game]]]
   :navigate :home}))
