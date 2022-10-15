(ns word-families.game-page-e2e
  (:require-macros [word-families.macros :as m])
  (:require
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   ["playwright-core" :as pw]
   [word-families.db :as db]))

(def chromium (.-chromium pw))
(def browser (atom nil))

(use-fixtures :once
  {:before
   (fn []
     (t/async done
              (->
               (p/let [new-browser (.launch chromium)]
                 (reset! browser new-browser)
                 (println "Browser started"))
               (p/catch #(println "Error before suite: " (.-stack %)))
               (p/finally #(done)))))

   :after
   (fn []
     (t/async done
              (-> (p/then (.close @browser)
                          #(println "Browser closed"))
                  (p/catch #(println "Error after suite: " (.-stack %)))
                  (p/finally #(done)))))})

(defn load-settings [^js page]
  (.addInitScript  page
                  ;; FIXME: Map should be a param
                   #(set! (.-settings js/window) (m/stringify {::db/groups [{::db/name "Terre"
                                                                             ::db/members ["Enterrer" "Terrien"]}
                                                                            {::db/name "Dent"
                                                                             ::db/members ["Dentiste" "Dentelle"]}
                                                                            {::db/name "Autre"
                                                                             ::db/members ["Tourteau" "Terminer"]}]}))))

(def correct-answers {"Enterrer" "Terre"
                      "Terrien" "Terre"
                      "Dentiste" "Dent"
                      "Dentelle" "Dent"
                      "Tourteau" "Autre"
                      "Terminer" "Autre"})

(def incorrect-answers {"Enterrer" "Dent"
                        "Dentiste" "Terre"

                        "Terrien" "Terre"
                        "Dentelle" "Dent"
                        "Tourteau" "Autre"
                        "Terminer" "Autre"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-question-elements [^js locatorizable] (.locator locatorizable "fieldset"))

(defn as_seq [^js locator]
  (p/let [count (.count locator)]
    (for [index (range count)] (.nth locator index))))

(defn collect-question-labels [^js locatorizable]
  (p/let [^js label-elements  (.locator locatorizable "legend")
          label-texts (.allInnerTexts label-elements)]
    (into #{} label-texts)))

(defn collect-answers-values [^js locatorizable]
  (p/let [locators (as_seq (.locator locatorizable "input[type=\"radio\"]"))]
    (->
     (map #(.getAttribute % "value") locators)
     (p/all)
     (p/then (fn [values] (into #{} values))))))

(defn get-submit-button [^js locatorizable]
  (.locator locatorizable "input[type=\"submit\"]"))

(defn check-radio-button [^js page [name value]]
  (-> page
      (.locator (str " [name=\"" name "\"][value=\"" value "\"]"))
      (.check #js {:force true})))

(defn fill-form [page answers]
  (->
   (p/all
    (map (partial check-radio-button page) answers))
   (p/then (fn [_] page))))

(defn submit-form [page]
  (let [submit-button (get-submit-button page)]
    (p/then (.click submit-button)
            (fn [_] page))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn assert-submit-button-disabled [page disabled?]
  (let [submit-button (get-submit-button page)]
    (p/then (.getAttribute submit-button "disabled")
            #(t/is (= disabled? (boolean %))))))

(defn assert-success-message-is-displayed [^js page]
  (p/let [visible-success-message? (.isVisible (.locator page "role=status" #js {:hasText "gagné"}))]

    (t/is (= true visible-success-message?))

    page))

(defn assert-success-message-is-not-displayed [^js page]
  (p/let [visible-success-message? (.isVisible (.locator page "role=status" #js {:hasText "gagné"}))]

    (t/is (= false visible-success-message?))

    page))

(defn assert-failure-message-is-displayed [^js page]
  (p/let [visible-failure-message?  (.isVisible (.locator page "role=status" #js {:hasText "erreurs"}))]

    (t/is (= true visible-failure-message?))

    page))

(defn assert-failure-message-is-not-displayed [^js page]
  (p/let [visible-failure-message?  (.isVisible (.locator page "role=status" #js {:hasText "erreurs"}))]

    (t/is (= false visible-failure-message?))

    page))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: run server with fixtures
(t/deftest displays-game-form
  (let [expected-question-labels #{"Enterrer" "Terrien" "Dentiste" "Dentelle" "Tourteau" "Terminer"}
        expected-answer-values #{"Terre" "Dent" "Autre"}]

    (t/async done
             (->
              (p/let [^js page (.newPage ^js @browser)]
                (load-settings page)
                (.goto page "http://localhost:8080")

                (t/testing "elements"
                  (p/let [question-elements (get-question-elements page)]

                    (t/testing "labels"
                      (p/let [actual-question-labels (collect-question-labels question-elements)]
                        (t/is (= expected-question-labels actual-question-labels))))

                    (t/testing "answers"
                      (p/let [locators (as_seq question-elements)]
                        (p/all
                         (for [question-element locators]
                           (p/let [actual-answer-values (collect-answers-values question-element)]
                             (t/is (= expected-answer-values actual-answer-values))))))))))

              (p/catch #(println (.-stack %)))
              (p/finally #(done))))))

(t/deftest initially-disabled-submit-button
  (t/async
   done
   (->
    (p/let [^js page (.newPage ^js @browser)]
      (load-settings page)
      (.goto page "http://localhost:8080")

      (assert-submit-button-disabled  page true))

    (p/catch #(println (.-stack %)))
    (p/finally #(done)))))

(t/deftest filling-form-enables-submit-button
  (t/async
   done
   (->
    (p/let [^js page (.newPage ^js @browser)]
      (load-settings page)
      (.goto page "http://localhost:8080")

      (p/->
       (fill-form page correct-answers)
       (assert-submit-button-disabled false)))

    (p/catch #(println (.-stack %)))
    (p/finally #(done)))))

(t/deftest user-submit-correct-answer
  (t/async
   done
   (->
    (p/let [^js page (.newPage ^js @browser)]
      (load-settings page)
      (.goto page "http://localhost:8080")

      (p/-> page
            (assert-success-message-is-not-displayed)
            (assert-failure-message-is-not-displayed)

            (fill-form correct-answers)
            (submit-form)

            (assert-success-message-is-displayed)
            (assert-failure-message-is-not-displayed)))

    (p/catch #(println (.-stack %)))
    (p/finally #(done)))))

(t/deftest user-submit-incorrect-answer
  (t/async
   done
   (->
    (p/let [^js page (.newPage ^js @browser)]
      (load-settings page)
      (.goto page "http://localhost:8080")

      (p/-> page
            (assert-success-message-is-not-displayed)
            (assert-failure-message-is-not-displayed)

            (fill-form incorrect-answers)
            (submit-form)

            (assert-failure-message-is-displayed)
            (assert-success-message-is-not-displayed)))

    (p/catch #(println (.-stack %)))
    (p/finally #(done)))))
