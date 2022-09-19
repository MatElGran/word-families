(ns word-families.basic-e2e
  (:require-macros [word-families.macros :as m])
  (:require
   [cljs.test :as t]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ["playwright-core" :as pw]
   [word-families.db :as db]))

(def chromium (.-chromium pw))

;; FIXME: Can't find how to use this with macro expansion
;; (def test-families [{::db/name "Terre"
;;                      ::db/words ["Enterrer" "Terrien"]}
;;                     {::db/name "Dent"
;;                      ::db/words ["Dentiste" "Dentelle"]}])
;;
;; (def test-anomalies ["Tourteau" "Terminer"])
;;
;; (def test-game {::db/current-game
;;                 {::db/families [{::db/name "Terre"
;;                                  ::db/words ["Enterrer" "Terrien"]}
;;                                 {::db/name "Dent"
;;                                  ::db/words ["Dentiste" "Dentelle"]}]
;;                  ::db/anomalies ["Tourteau" "Terminer"]}})

;; Go blocks remove type hints, so we have to extract these functions from the test
(defn launch-browser [] (.launch ^js chromium))
(defn new-context [browser] (.newContext ^js browser))
(defn new-page [context] (.newPage ^js context))
(defn goto [page, url] (.goto ^js page url))
(defn locator [page selector] (.locator ^js page selector))
(defn all-inner-texts
  [page selector]
  (let [locator (locator page selector)]
    (.allInnerTexts locator)))

(defn load-settings [page]
  (.on ^js page "console" #(println (.text ^js %)))
  (.addInitScript ^js page
                  #(set! (.-settings js/window) (m/stringify {::db/current-game
                                                             {::db/families [{::db/name "Terre"
                                                                              ::db/words ["Enterrer" "Terrien"]}
                                                                             {::db/name "Dent"
                                                                              ::db/words ["Dentiste" "Dentelle"]}]
                                                              ::db/anomalies ["Tourteau" "Terminer"]}}))))

(t/deftest basic-test-2
  (t/async done
           (go
             (let [browser (<p! (launch-browser))
                   context (<p! (new-context browser))
                   page (<p! (new-page context))]
               (try
                 (<p! (load-settings page))
                 (<p! (goto page "http://localhost:8080"))

                 (let [expected-words #{"Enterrer" "Terrien" "Dentiste" "Dentelle" "Tourteau" "Terminer"}
                       actual-words (into #{} (<p! (all-inner-texts page ".label")))]
                   (t/is (= expected-words actual-words)))

                 (catch js/Error err (do
                                       (println (ex-cause err))
                                       ;; TODO: this can be more detailed,
                                       ;; cf https://cljs.github.io/api/cljs.test/try-expr
                                       (t/report {:type :error}))))
               (.close browser)
               (done)))))
