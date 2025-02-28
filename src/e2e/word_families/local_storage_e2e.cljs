(ns word-families.local-storage-e2e
  (:require
   ["playwright-core" :as pw]
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   [word-families.group :as group]
   [word-families.settings.core :as settings-core]
   [word-families.settings.spec :as settings-spec]
   [word-families.test-helpers :as test-helpers]
   [word-families.pages.settings :as page]))

(def chromium (.-chromium pw))
(def browser (atom nil))
(def url "http://localhost:8080/settings")

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

(def default-group-names (into #{} (map ::group/name settings-core/default-groups)))

(t/deftest load-default-settings-when-nothing-persisted
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser)]
      (.goto page url)

      (p/let [group-elements (page/get-group-elements page)
              actual-groups (page/collect-group-names group-elements)]
        (t/is (= default-group-names actual-groups))))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest load-default-settings-when-invalid-edn-persisted
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser "{")]
      (.goto page url)

      (p/let [group-elements (page/get-group-elements page)
              actual-groups (page/collect-group-names group-elements)]
        (t/is (= default-group-names actual-groups))))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest load-default-settings-when-persisted-data-has-invalid-format
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser {::settings-spec/groups [{:name "Terre"}]})]
      (.goto page url)

      (p/let [group-elements (page/get-group-elements page)
              actual-groups (page/collect-group-names group-elements)]
        (t/is (= default-group-names actual-groups))))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))
