(ns word-families.settings-page-e2e
  (:require
   ["playwright-core" :as pw]
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   [word-families.test-helpers :as test-helpers]))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-group-elements [^js locatorizable] (.locator locatorizable ".group"))

(defn collect-group-names [^js locatorizable]
  (p/let [^js group-headers  (.locator locatorizable "h3")
          group-names (.allInnerTexts group-headers)]
    (into #{} group-names)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: run server with fixtures
(t/deftest displays-game-form
  (let [expected-groups #{"Terre" "Dent" "Autre"}]

    (t/async
     done
     (->
      (p/let [^js page (.newPage ^js @browser)]
        (test-helpers/load-settings page)
        (.goto page "http://localhost:8080/settings")

        (p/let [group-elements (get-group-elements page)
                actual-groups (collect-group-names group-elements)]
          (t/is (= expected-groups actual-groups))))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))
