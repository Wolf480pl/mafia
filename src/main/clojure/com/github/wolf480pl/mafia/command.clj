(ns com.github.wolf480pl.mafia.command
    (:require [clojure.string :as str] 
              [com.github.wolf480pl.mafia.player :as ply :refer (sendMsg joinRoom getDefaultCommand getRoomRegistry)]
              [com.github.wolf480pl.mafia.room :refer (getRoom getNick claimNick leave broadcast isOwner)]))

(defrecord Command [name args])

(defmacro regCmd
    ([registry func] `(assoc ~registry ~(name func) ~func))
    ([registry cmdName func] `(assoc ~registry ~cmdName ~func)))

(defmacro regCmd->
    [registry & forms] `(-> ~registry ~@(map #(cons 'regCmd %1) forms)))

(defn dispatchCmd [registry, player, cmd]
    (let [cmdName (or (.name cmd) (getDefaultCommand player))
          cmdfn (registry cmdName)]
        (if cmdfn
            (apply cmdfn player (.args cmd))
            (sendMsg player (format "Error: No such command: %s" cmdName)))))

(defn strRest [args]
    (str/join " " args))

(defn echo [player & args]
    (sendMsg player (strRest args)))

(defn join [player room]
    (joinRoom player (getRoom (getRoomRegistry player) room player)))

(defn say [player & args]
    (let [room (ply/getRoom player)]
        (if room
            (broadcast room (format "<%s> %s" (getNick room player) (strRest args)))
            (sendMsg player "Can't speak while not in any room"))))

(defn nick [player newNick]
    (let [room (ply/getRoom player)]
        (claimNick room newNick player)))

(defn kick [player target]
    (let [room (ply/getRoom player)]
        (if (isOwner room player)
            (do
              (broadcast room (format "%s kicks %s" (getNick room player) target))
              (leave room target))
            (sendMsg player "Can't kick: Permission Denied"))))

(defn regStandardCommands [registry]
    (regCmd-> registry (echo) (join) (nick) (say) (kick)))
