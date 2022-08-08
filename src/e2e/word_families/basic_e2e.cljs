(ns word-families.basic-e2e
  (:require [cljs.test :as t]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            ["playwright-core" :as pw]))

(def chromium (.-chromium pw))


;; Go blocks remove type hints, so we have to extract these functions from the test
(defn launch-browser [] (.launch ^js chromium))
(defn new-context [browser] (.newContext ^js browser))
(defn new-page [context] (.newPage ^js context))
(defn goto [page, url] (.goto ^js page url))

(t/deftest basic-test-2
  (t/async done
           (go
             (let [browser (<p! (launch-browser))
                   context (<p! (new-context browser))
                   page (<p! (new-page context))]
               (try
                 (<p! (goto page "http://localhost:8080"))
                 (let [text (<p! (.innerText page "#root"))]
                   (t/is (= text "Word families")))
                 (catch js/Error err (println (ex-message err))))
               (.close browser)
               (done)))))
