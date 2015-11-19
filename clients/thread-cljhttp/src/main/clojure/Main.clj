(ns Main
  (:use FiberCljHttpEnv)
  (:import (co.paralleluniverse.comsat.bench.http.client ClientBase))
  (:gen-class
    :extends co.paralleluniverse.comsat.bench.http.client.ClientBase
    :main
    true))

(defn -setupEnv [_ _]
  (FiberCljHttpEnv.))

(defn -main [& args]
  (.run (Main.) (into-array String args) (ClientBase/CACHED_THREAD_SF)))
