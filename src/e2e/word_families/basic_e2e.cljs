(ns word-families.basic-e2e
  (:require-macros [word-families.macros :as m])
  (:require
   [cljs.test :as t]
   [promesa.core :as p]
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

(defn launch-browser [] (.launch ^js chromium))
(defn new-page [browser] (.newPage ^js browser))
(defn goto [page, url] (.goto ^js page url))
(defn locator [page selector] (.locator ^js page selector))
(defn get-question-elements [page] (locator page ".field"))
(defn get-word-elements [page] (locator page ".label"))
(defn all-inner-texts
  [locator ]
    (.allInnerTexts ^js locator))

(defn load-settings [page]
  (.on ^js page "console" #(println (.text ^js %)))
  (.addInitScript ^js page
                  #(set! (.-settings js/window) (m/stringify {::db/current-game
                                                             {::db/families [{::db/name "Terre"
                                                                              ::db/words ["Enterrer" "Terrien"]}
                                                                             {::db/name "Dent"
                                                                              ::db/words ["Dentiste" "Dentelle"]}]
                                                              ::db/anomalies ["Tourteau" "Terminer"]}}))))

;; FIXME: run server with fixtures
(t/deftest displays-preloaded-settings-correctly
  (t/async done
           (->
            (p/let [browser (launch-browser)
                    page (new-page browser)]
              (p/do
                (load-settings page)
                (goto page "http://localhost:8080"))
              (p/let [expected-words #{"Enterrer" "Terrien" "Dentiste" "Dentelle" "Tourteau" "Terminer"}
                      question-elements (get-question-elements page)
                      word-elements (get-word-elements question-elements)
                      actual-words (all-inner-texts word-elements)]

                (t/is (= expected-words (into #{} actual-words)))))

            (p/catch #(println %))
            (p/finally #(done)))))
