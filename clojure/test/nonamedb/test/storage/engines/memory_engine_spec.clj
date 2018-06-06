(ns nonamedb.test.storage.engines.memory-engine-spec
  (:require
    [clojure.test :refer :all]
    [nonamedb.storage.engines.memory-engine :refer [get-from put-into create-engine]]))

(def test-key "some key")
(def test-value (byte-array (map byte "some value")))
(def updated-test-value (byte-array (map byte "some updated value")))

(deftest should-fail-to-retrieve-missing-data
  (let [engine (create-engine)]
    (is (nil? (get-from engine test-key)))
    (is (nil? (get-from engine "missing-data")))))

(deftest should-successfully-retrieve-data
  (let [engine (create-engine)]
    (is (put-into engine test-key test-value))
    (is (= (get-from engine test-key) test-value))))

(deftest should-successfully-retrieve-updated-data
  (let [engine (create-engine)]
    (is (put-into engine test-key test-value))
    (is (= (get-from engine test-key) test-value))
    (is (put-into engine test-key updated-test-value))
    (is (= (get-from engine test-key) updated-test-value))))

(deftest should-fail-to-retrieve-removed-data
  (let [engine (create-engine)]
    (is (put-into engine test-key test-value))
    (is (= (get-from engine test-key) test-value))
    (is (put-into engine test-key (byte-array [])))
    (is (nil? (get-from engine test-key)))))
