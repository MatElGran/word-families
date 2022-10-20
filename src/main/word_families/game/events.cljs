(ns word-families.game.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.game.spec :as spec]
   [word-families.lib :as lib]))

(lib/reg-event-game
 ::register-answer
 (fn-traced
  [game [_ groupable group]]
  (assoc-in game [::spec/answers groupable] group)))

(lib/reg-event-game
 ::validate-answers
 (fn-traced
  [game _]
  (let [actual-answers (::spec/answers game)
        expected-answers  (::spec/expected-answers game)
        error? (fn [answer] (not (= (get answer 1) (get expected-answers (get answer 0)))))
        errors (into {} (filter error? actual-answers))]
    (assoc game ::spec/verified? true ::spec/errors errors))))
