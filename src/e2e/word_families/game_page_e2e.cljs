(ns word-families.game-page-e2e
  (:require
   ["playwright-core" :as pw]
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   [word-families.settings.spec :as spec]
   [word-families.test-helpers :as test-helpers]))

(def chromium (.-chromium pw))
(def browser (atom nil))

(def local-settings {::spec/groups
                     [{::spec/name "Terre"
                       ::spec/members ["Enterrer" "Terrien"]
                       ::spec/traps ["Terminer"]}
                      {::spec/name "Dent"
                       ::spec/members ["Dentiste" "Dentelle"]
                       ::spec/traps ["Accident"]}]})

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

(def correct-answers {"Enterrer" "Terre"
                      "Terrien" "Terre"
                      "Dentiste" "Dent"
                      "Dentelle" "Dent"
                      "Accident" "Autre"
                      "Terminer" "Autre"})

(def errors {"Enterrer" "Dent"
             "Dentiste" "Terre"})

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
      (.locator (str "input[name=" name "][value=" value "]"))
      (.click #js {:force true})))

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
    (test-helpers/after 20
           #(p/then (.getAttribute submit-button "disabled")
                    (fn [attribute] (t/is (= disabled? (boolean attribute))))))))

(defn assert-success-message-is-displayed [^js page]
  (p/then
   (test-helpers/assert-visible (.locator page "role=status" #js {:hasText "gagné"}))
   (constantly page)))

(defn assert-success-message-is-not-displayed [^js page]
  (p/then
   (test-helpers/assert-not-visible (.locator page "role=status" #js {:hasText "gagné"}))
   (constantly page)))

(defn assert-failure-message-is-displayed [^js page]
  (p/then
   (test-helpers/assert-visible (.locator page "role=status" #js {:hasText "erreurs"}))
   (constantly page)))

(defn assert-failure-message-is-not-displayed [^js page]
  (p/then
   (test-helpers/assert-not-visible (.locator page "role=status" #js {:hasText "erreurs"}))
   (constantly page)))

(defn assert-errors-are-displayed [^js page errors]
  (p/then
   (p/all
    (map
     #(test-helpers/assert-visible (.locator page (str "[aria-invalid=true][name=" (get % 0) "][value=" (get % 1) "]")))
     errors))
   (constantly page)))

(defn assert-no-errors-are-displayed [^js page]
  (p/then
   (test-helpers/assert-not-visible (.locator page "[aria-invalid=true]"))
   (constantly page)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: run server with fixtures
(t/deftest displays-game-form
  (let [expected-question-labels #{"Enterrer" "Terrien" "Dentiste" "Dentelle" "Accident" "Terminer"}
        expected-answer-values #{"Terre" "Dent" "Autre"}]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
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

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest initially-disabled-submit-button
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page "http://localhost:8080")

      (assert-submit-button-disabled  page true))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest filling-form-enables-submit-button
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page "http://localhost:8080")

      (p/->
       (fill-form page correct-answers)
       (assert-submit-button-disabled false)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest user-submit-correct-answer
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser , local-settings)]
      (.goto page "http://localhost:8080")

      (p/-> page
            (assert-success-message-is-not-displayed)
            (assert-failure-message-is-not-displayed)

            (fill-form correct-answers)
            (submit-form)

            (assert-success-message-is-displayed)
            (assert-failure-message-is-not-displayed)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest user-submit-incorrect-answer
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page "http://localhost:8080")

      (p/-> page
            (assert-success-message-is-not-displayed)
            (assert-failure-message-is-not-displayed)

            (fill-form (merge correct-answers errors))
            (submit-form)

            (assert-failure-message-is-displayed)
            (assert-success-message-is-not-displayed)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest incorrect-answer-are-displayed
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page "http://localhost:8080")

      (p/-> page
            (assert-no-errors-are-displayed)

            (fill-form (merge correct-answers errors))
            (submit-form)

            (assert-errors-are-displayed errors)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))
