(ns word-families.settings-page-e2e
  (:require
   ["playwright-core" :as pw]
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   [word-families.test-helpers :as test-helpers]
   [word-families.pages.settings :as page]))

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

(defn assert-each-group-has-a-delete-button [^js page]
  (p/let [group-elements (page/get-group-elements page)
          group-count (.count group-elements)
          delete-buttons (page/get-delete-buttons group-elements)
          delete-button-count (.count delete-buttons)]
    (t/is (= group-count delete-button-count))
    page))

(defn assert-group-is-visible [^js page group-name]
  (p/let [group-elements (page/get-group-elements page)
          group-element (.filter group-elements #js {:hasText group-name})]

    (p/then (test-helpers/assert-visible group-element)
            (constantly page))))

(defn assert-group-is-not-visible [^js page group-name]
  (p/let [group-elements (page/get-group-elements page)
          group-element (.filter group-elements #js {:hasText group-name})]

    (p/then (test-helpers/assert-not-visible group-element)
            (constantly page))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: run server with fixtures
(t/deftest displays-group-list
  (let [expected-groups #{"Terre" "Dent"}]

    (t/async
     done
     (->
      (p/let [^js page (.newPage ^js @browser)]
        (test-helpers/load-settings page)
        (.goto page "http://localhost:8080/settings")

        (p/let [group-elements (page/get-group-elements page)
                actual-groups (page/collect-group-names group-elements)]
          (t/is (= expected-groups actual-groups))))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest user-can-delete-group
  (let [groups ["Terre" "Dent"]
        group-to-delete (first groups)
        remaining-group (last groups)]

    (t/async
     done
     (->
      (p/let [^js page (.newPage ^js @browser)]
        (test-helpers/load-settings page)
        (.goto page "http://localhost:8080/settings")

        (p/-> page
              (assert-each-group-has-a-delete-button)
              (page/click-delete-button group-to-delete)
              (assert-group-is-not-visible group-to-delete)
              (assert-group-is-visible remaining-group)))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest group-deletion-is-persisted-across-reloads
  (let [groups ["Terre" "Dent"]
        group-to-delete (first groups)
        remaining-group (last groups)]

    (t/async
     done
     (->
      (p/let [^js page (.newPage ^js @browser)]
        (test-helpers/load-settings page)
        (.goto page "http://localhost:8080/settings")

        (p/-> page
              (page/click-delete-button group-to-delete)
              (test-helpers/reload-page)
              (assert-group-is-not-visible group-to-delete)
              (assert-group-is-visible remaining-group)))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))
