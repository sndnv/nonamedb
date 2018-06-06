(ns nonamedb.test.api.service-spec
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [nonamedb.storage.engines.memory-engine :refer [create-engine]]
            [nonamedb.api.service :refer [create-service]]))

(def test-key "some_key")
(def test-value (byte-array (map byte "some value")))
(def updated-test-value (byte-array (map byte "some updated value")))

(deftest should-fail-to-retrieve-missing-data
  (let [service (create-service (create-engine))]
    (is (= (service (mock/request :get (str "/" test-key)))
           {:status 404}))

    (is (= (service (mock/request :get (str "/missing-data")))
           {:status 404}))

    (is (= (service (mock/request :get "/"))
           {:status 404}))))

(deftest should-successfully-retrieve-data
  (let [service (create-service (create-engine))]
    (is (= (service (-> (mock/request :post (str "/" test-key))
                        (mock/body test-value)))
           {:status  200
            :headers {}
            :body    ""}))
    (is (= (service (mock/request :get (str "/" test-key)))
           {:status       200,
            :headers      {},
            :body         "some value",
            :content-type "application/octet-stream"}))))

(deftest should-successfully-retrieve-updated-data
  (let [service (create-service (create-engine))]
    (is (= (service (-> (mock/request :post (str "/" test-key))
                        (mock/body test-value)))
           {:status  200
            :headers {}
            :body    ""}))
    (is (= (service (mock/request :get (str "/" test-key)))
           {:status       200,
            :headers      {},
            :body         "some value",
            :content-type "application/octet-stream"}))
    (is (= (service (-> (mock/request :put (str "/" test-key))
                        (mock/body updated-test-value)))
           {:status  200
            :headers {}
            :body    ""}))
    (is (= (service (mock/request :get (str "/" test-key)))
           {:status       200,
            :headers      {},
            :body         "some updated value",
            :content-type "application/octet-stream"}))))

(deftest should-fail-to-retrieve-removed-data
  (let [service (create-service (create-engine))]
    (is (= (service (-> (mock/request :post (str "/" test-key))
                        (mock/body test-value)))
           {:status  200
            :headers {}
            :body    ""}))
    (is (= (service (mock/request :get (str "/" test-key)))
           {:status       200,
            :headers      {},
            :body         "some value",
            :content-type "application/octet-stream"}))
    (is (= (service (mock/request :delete (str "/" test-key)))
           {:status  200
            :headers {}
            :body    ""}))
    (is (= (service (mock/request :get (str "/" test-key)))
           {:status 404}))))
