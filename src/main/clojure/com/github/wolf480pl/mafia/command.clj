(ns com.github.wolf480pl.mafia.command
    (:require [clojure.string :as str] 
              [com.github.wolf480pl.mafia.player :refer (sendMsg)]))

(defrecord Command [name args])

(defmacro regCmd
    ([registry func] `(assoc ~registry ~(name func) ~func))
    ([registry cmdName func] `(assoc ~registry ~cmdName ~func)))

(defmacro regCmd->
    [registry & forms] `(-> registry ~@(map #(cons 'regCmd %1) forms)))

(defn dispatchCmd [registry, player, cmd]
    (let [cmdName (or (.name cmd) (:defaultCmd @(.state player)))
          cmdfn (registry cmdName)]
        (if cmdfn
            (apply cmdfn player (.args cmd))
            (sendMsg player (format "Error: No such command: %s" cmdName)))))

(defn echo [player & args]
    (sendMsg player (str/join " " args)))

(defn regStandardCommands [registry]
    (regCmd registry echo))
