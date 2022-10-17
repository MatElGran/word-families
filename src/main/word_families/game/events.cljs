(ns word-families.game.events
  (:require
   [day8.re-frame.tracing :refer [fn-traced]]
   [word-families.core :as core]
   [word-families.db :as db]))

(core/reg-event-db
 ::register-answer
 (fn-traced
  [db [_ groupable group]]
  (assoc-in db [::db/current-game ::db/answers groupable] group)))

(core/reg-event-db
 ::validate-answers
 (fn-traced
  [db _]
  (let [actual-answers (get-in db [::db/current-game ::db/answers])
        expected-answers (get-in db [::db/current-game ::db/expected-answers])
        error? (fn [answer] (not (= (get answer 1) (get expected-answers (get answer 0)))))
        errors (into {} (filter error? actual-answers))]
    (-> db
        (assoc-in [::db/current-game ::db/verified?] true)
        (assoc-in [::db/current-game ::db/errors] errors)))))
