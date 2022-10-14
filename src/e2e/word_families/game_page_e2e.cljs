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
                   #(set! (.-settings js/window) (m/stringify {::db/current-game
                                                               {::db/groups [{::db/name "Terre"
                                                                              ::db/members ["Enterrer" "Terrien"]}
                                                                             {::db/name "Dent"
                                                                              ::db/members ["Dentiste" "Dentelle"]}
                                                                             {::db/name "Autre"
                                                                              ::db/members ["Tourteau" "Terminer"]}]}}))))

(defn get-question-elements [^js locatorizable] (.locator locatorizable ".field"))

(defn as_seq [^js locator]
  (p/let [count (.count locator)]
    (for [index (range count)] (.nth locator index))))

(defn collect-question-labels [^js locatorizable]
  (p/let [^js label-elements  (.locator locatorizable ".label")
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

(defn disabled? [input]
  (.getAttribute input "disabled"))

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
                             (t/is (= expected-answer-values actual-answer-values)))))))))

                (t/testing "button"
                  (p/let [submit-button (get-submit-button page)
                          disabled? (disabled? submit-button)]
                    (t/is disabled?))))

              (p/catch #(println (.-stack %)))
              (p/finally #(done))))))
