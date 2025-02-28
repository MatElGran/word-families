(ns word-families.routing-e2e
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

(t/deftest root-path-displays-home-panel
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser)]
      (.goto page "http://localhost:8080")
      (p/let [actual-panel-id (.getAttribute (.locator page "#panel-root") "data-test-id")]
        (t/is (= "home-panel" actual-panel-id))))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest game-path-displays-game-panel
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser)]
      (.goto page "http://localhost:8080/game")
      (p/let [actual-panel-id (.getAttribute (.locator page "#panel-root") "data-test-id")]
        (t/is (= "game-panel" actual-panel-id))))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest settings-path-displays-settings-panel
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser)]
      (.goto page "http://localhost:8080/settings")
      (p/let [actual-panel-id (.getAttribute (.locator page "#panel-root") "data-test-id")]
        (t/is (= "settings-panel" actual-panel-id))))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))
