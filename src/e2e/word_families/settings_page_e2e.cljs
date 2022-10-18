(ns word-families.settings-page-e2e
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
        (load-settings page)
        (.goto page "http://localhost:8080/settings")

        (p/let [group-elements (get-group-elements page)
                actual-groups (collect-group-names group-elements)]
          (t/is (= expected-groups actual-groups))))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))
