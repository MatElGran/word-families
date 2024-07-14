(ns word-families.settings-page-e2e
  (:require
   ["playwright-core" :as pw]
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   [word-families.group :as group]
   [word-families.pages.settings :as page]
   [word-families.settings.spec :as spec]
   [word-families.test-helpers :as test-helpers]))

(def chromium (.-chromium pw))
(def browser (atom nil))
(def url "http://localhost:8080/settings")
(def local-settings {::spec/groups
                     [(group/init "Terre"
                                  ["Enterrer" "Terrien"]
                                  ["Terminer"])
                      (group/init "Dent"
                                  ["Dentiste" "Dentelle"]
                                  ["Accident"])]})

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

(defn assert-form-is-visible [^js page]
  (p/let [form-element (page/get-form-element page)]

    (p/then (test-helpers/assert-visible form-element)
            (constantly page))))

(defn assert-form-is-not-visible [^js page]
  (p/let [form-element (page/get-form-element page)]

    (p/then (test-helpers/assert-not-visible form-element)
            (constantly page))))

(defn assert-group-has-been-modified [^js page existing-groups original-group new-group-name]
  (p/let [unchanged-group-names (map ::group/name
                                     (remove #(= (::group/name %) (::group/name original-group)) existing-groups))
          expected-group-names (into #{} (conj unchanged-group-names new-group-name))
          group-elements (page/get-group-elements page)
          actual-groups (page/collect-group-names group-elements)]

    (t/is (= expected-group-names actual-groups))
    page))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: run server with fixtures
(t/deftest displays-group-list
  (let [expected-group-names (into #{} (map ::group/name (::spec/groups local-settings)))]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
        (.goto page url)

        (p/let [group-elements (page/get-group-elements page)
                actual-groups (page/collect-group-names group-elements)]
          (t/is (= expected-group-names actual-groups))))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest user-can-delete-group
  (let [group-names (map ::group/name (::spec/groups local-settings))
        group-to-delete (first group-names)
        remaining-group (last group-names)]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
        (.goto page url)

        (p/-> page
              (page/click-modify-group-button group-to-delete)
              (page/click-delete-group-button group-to-delete)
              (assert-group-is-not-visible group-to-delete)
              (assert-group-is-visible remaining-group)))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest group-deletion-is-persisted-across-reloads
  (let [group-names (map ::group/name (::spec/groups local-settings))
        group-to-delete (first group-names)
        remaining-group (last group-names)]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
        (.goto page url)

        (p/-> page
              (page/click-modify-group-button group-to-delete)
              (page/click-delete-group-button group-to-delete)
              (test-helpers/reload-page)
              (assert-group-is-not-visible group-to-delete)
              (assert-group-is-visible remaining-group)))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest user-can-modify-existing-new-group
  (let [original-group (get (::spec/groups local-settings) 0)
        updated-group-name (str (::group/name original-group) "-updated")]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
        (.goto page "http://localhost:8080/settings")

        (p/-> page
              (assert-form-is-not-visible)
              (page/click-modify-group-button (::group/name original-group))
              (assert-form-is-visible)
              (page/fill-group-name updated-group-name)
              (page/click-submit-button)
              (assert-form-is-not-visible)
              (assert-group-has-been-modified (::spec/groups local-settings) original-group updated-group-name)))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest group-modification-is-persisted-across-reloads
  (let [original-group (get (::spec/groups local-settings) 0)
        updated-group-name (str (::group/name original-group) "-updated")]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
        (.goto page "http://localhost:8080/settings")

        (p/-> page
              (page/click-modify-group-button (::group/name original-group))
              (page/fill-group-name updated-group-name)
              (page/click-submit-button)
              (test-helpers/reload-page)
              (assert-group-has-been-modified (::spec/groups local-settings) original-group updated-group-name)))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))
