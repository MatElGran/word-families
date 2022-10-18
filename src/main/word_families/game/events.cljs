(ns word-families.game.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.lib :as lib]
   [word-families.db :as db]))

(lib/reg-event-game
 ::register-answer
 (fn-traced
  [game [_ groupable group]]
  (assoc-in game [::db/answers groupable] group)))

(lib/reg-event-game
 ::validate-answers
 (fn-traced
  [game _]
  (let [actual-answers (::db/answers game)
        expected-answers  (::db/expected-answers game)
        error? (fn [answer] (not (= (get answer 1) (get expected-answers (get answer 0)))))
        errors (into {} (filter error? actual-answers))]
    (assoc game ::db/verified? true ::db/errors errors))))
