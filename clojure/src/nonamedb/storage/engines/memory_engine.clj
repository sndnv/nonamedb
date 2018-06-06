(ns nonamedb.storage.engines.memory-engine
  (:require [taoensso.timbre :as log])
  (:use [co.paralleluniverse.pulsar core actors])
  (:refer-clojure :exclude [promise await]))

(defsfn
  store-actor []
  (loop [store {}]
    (recur
      (receive
        [from tag [:get k]]
        (let [result (get store k)]
          (log/debug "[GET] Value with key [" k "]" (if (nil? result) "not found" "found"))
          (! from tag [:store-result result])
          store)

        [from tag [:put k v]]
        (if (and (not (nil? v))
                 (instance? (type (byte-array [])) v)
                 (not (empty? v)))
          (do
            (log/debug "[PUT]" (if (contains? store k) "Updating" "Adding") "value with key [" k "]")
            (! from tag [:store-result true])
            (assoc store k v))
          (do
            (log/debug "[PUT] Removing value with key [" k "]")
            (! from tag [:store-result true])
            (dissoc store k)))))))

(defsfn
  request-actor [tag timeout]
  (receive
    [tag [:store-result result]] result
    :after timeout nil))

(defn create-engine []
  (spawn store-actor))

(defn get-from
  ([engine k] (get-from engine k 1000))

  ([engine k timeout]
   (let [tag (maketag)
         request-actor (spawn request-actor tag timeout)]
     (! engine request-actor tag [:get k])
     (join request-actor))))

(defn put-into
  ([engine k v] (put-into engine k v 1000))

  ([engine k v timeout]
   (let [tag (maketag)
         request-actor (spawn request-actor tag timeout)]
     (! engine request-actor tag [:put k v])
     (join request-actor))))
