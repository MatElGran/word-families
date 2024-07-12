(ns word-families.game-page-e2e
  (:require
   ["playwright-core" :as pw]
   [cljs.test :as t :refer-macros [use-fixtures]]
   [promesa.core :as p]
   [word-families.group :as group]
   [word-families.settings.spec :as spec]
   [word-families.test-helpers :as test-helpers]))

(def chromium (.-chromium pw))
(def browser (atom nil))

(def ^:private url
  "http://localhost:8080/game")

(def local-settings {::spec/groups
                     [(group/init "Terre"
                                  ["Enterrer" "Terrien"]
                                  ["Terminer"])
                      (group/init "Dent"
                                  ["Dentiste" "Dentelle"]
                                  ["Accident"])]})

;; FIXME: run the server automatically from here
;; FIXME: Try running this once per run instead of per file
(use-fixtures :once
  {:before
   (fn []
     (t/async done
              (->
                ;; FIXME: Set headless mode with an ENV var
               (p/let [new-browser (.launch chromium #js {:headless true})]
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

;; FIXME: Duplicates injected local settings
(def correct-groups [{::group/name "Terre"
                      ::group/members [{::group/name "Enterrer"} {::group/name "Terrien"}]}
                     {::group/name "Dent"
                      ::group/members [{::group/name "Dentiste"} {::group/name "Dentelle"}]}
                     {::group/name "Autre"
                      ::group/members [{::group/name "Terminer"} {::group/name "Accident"}]}])

(def incorrect-groups [{::group/name "Terre"
                        ::group/members [{::group/name "Dentiste"} {::group/name "Dentelle"}]}
                       {::group/name "Dent"
                        ::group/members [{::group/name "Enterrer"} {::group/name "Terrien"}]}
                       {::group/name "Autre"
                        ::group/members [{::group/name "Terminer"} {::group/name "Accident"}]}])

;; FIXME: rename
(defn- make-corrector [expected-groups]
  (fn [actual-answer]
    (let [actual-group-name (val actual-answer)
          actual-groupable-name (key actual-answer)
          expected-groupable-names (get expected-groups actual-group-name)]

      (some #(= actual-groupable-name %) expected-groupable-names))))

;; FIXME: This duplicates game logic, it should be at least partially exposed by the game module
(def errors
  (let [actual-members (into {} (map
                                 (fn [actual-group]
                                   [(::group/name actual-group)
                                    (map ::group/name (::group/members actual-group))])
                                 incorrect-groups))
        expected-groups (into {} (map
                                  (fn [expected-group]
                                    [(::group/name expected-group)
                                     (map ::group/name (::group/members expected-group))])
                                  correct-groups))
        incorrect? (complement (make-corrector expected-groups))]

    (->> actual-members
         (map
          (fn [actual-group]
            (let [groupable-names (val actual-group)
                  group-name (key actual-group)
                  actual-pairs (zipmap groupable-names (repeat group-name))]

              (into {} (filter incorrect? actual-pairs)))))
         (into {}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- get-groups-container [^js page] (.locator page ".group-cards"))

(defn- get-groupables-container [^js page] (.locator page ".groupable-cards"))

(defn- get-container-cards [^js container] (.locator container ".box"))

(defn get-group-cards [^js locatorizable] (get-container-cards (get-groups-container locatorizable)))

(defn get-groupable-cards [^js locatorizable] (get-container-cards (get-groupables-container locatorizable)))

(defn- get-card-with-label [cards label]
  (.filter ^js cards #js {:hasText (::group/name label)}))

(defn collect-labels [^js label-elements]
  (p/let [label-texts (.allInnerTexts label-elements)]
    (into #{} label-texts)))

(defn get-submit-button [^js locatorizable]
  (.locator locatorizable "button[type=\"submit\"]"))

(defn select-groupable [^js groupable-cards groupable]
  (p/let [target-groupable-card (get-card-with-label groupable-cards groupable)]
    (.click target-groupable-card #js {:force true})))

(defn select-groupables [page group]
  (let [groupable-cards (get-groupable-cards page)
        groupables (::group/members group)]
    (p/all
     (map
      #(select-groupable groupable-cards %)
      groupables))))

(defn fill-form-for-group [^js page group]
  (p/let [group-cards (get-group-cards page)
          target-group-card (get-card-with-label group-cards group)]
    (p/then (.click target-group-card #js {:force true})
            (fn [_] (select-groupables page group)))))

(defn fill-form [^js page groups]
  (p/then (p/doseq [group groups] (fill-form-for-group page group))
          (constantly page)))

(defn submit-form [page]
  (let [submit-button (get-submit-button page)]
    (p/then (.click submit-button)
            (constantly page))))

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
    ;; FIXME: setup aria back
     #(test-helpers/assert-visible (.locator page ".groupable-cards.verified .incorrect" #js {:hasText (key %)}))
     errors))
   (constantly page)))

(defn assert-no-errors-are-displayed [^js page]
  (p/then
    ;; FIXME: setup aria back
   ;; (test-helpers/assert-not-visible (.locator page "[aria-invalid=true]"))
   (test-helpers/assert-not-visible (.locator page ".groupable-cards .incorrect"))
   (constantly page)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(t/deftest displays-game-form
  (let [expected-groupable-labels #{"Enterrer" "Terrien" "Dentiste" "Dentelle" "Accident" "Terminer"}
        expected-group-values #{"Terre" "Dent" "Autre"}]

    (t/async
     done
     (->
      (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
        (.goto page url)

        (t/testing "cards"

          (t/testing "groups"
            (p/let [group-cards (get-group-cards page)
                    actual-group-values (collect-labels group-cards)]
              (t/is (= expected-group-values actual-group-values))))

          (t/testing "groupables"
            (p/let [groupable-cards (get-groupable-cards page)]

              (p/let [actual-groupable-labels (collect-labels groupable-cards)]
                (t/is (= expected-groupable-labels actual-groupable-labels)))))))

      (p/catch (fn [error] (t/do-report {:type :error :actual error})))
      (p/finally #(done))))))

(t/deftest initially-disabled-submit-button
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page url)

      (assert-submit-button-disabled  page true))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest filling-form-enables-submit-button
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page url)

      (p/->
       (fill-form page correct-groups)
       (assert-submit-button-disabled false)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest user-submit-correct-group
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser , local-settings)]
      (.goto page url)

      (p/-> page
            (assert-success-message-is-not-displayed)
            (assert-failure-message-is-not-displayed)

            (fill-form correct-groups)
            (submit-form)

            (assert-success-message-is-displayed)
            (assert-failure-message-is-not-displayed)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest user-submit-incorrect-group
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page url)

      (p/-> page
            (assert-success-message-is-not-displayed)
            (assert-failure-message-is-not-displayed)

            (fill-form incorrect-groups)
            (submit-form)

            (assert-failure-message-is-displayed)
            (assert-success-message-is-not-displayed)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))

(t/deftest incorrect-group-are-displayed
  (t/async
   done
   (->
    (p/let [^js page (test-helpers/get-new-page ^js @browser local-settings)]
      (.goto page url)

      (p/-> page
            (assert-no-errors-are-displayed)

            (fill-form incorrect-groups)
            (submit-form)

            (assert-errors-are-displayed errors)))

    (p/catch (fn [error] (t/do-report {:type :error :actual error})))
    (p/finally #(done)))))
