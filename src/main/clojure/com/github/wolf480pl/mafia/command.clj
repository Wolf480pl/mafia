;
; Copyright (c) 2015 Wolf480pl <wolf480@interia.pl>
; This program is licensed under the GNU Lesser General Public License.
;
; This program is free software: you can redistribute it and/or modify
; it under the terms of the GNU Lesser General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU Lesser General Public License for more details.
;
; You should have received a copy of the GNU Lesser General Public License
; along with this program.  If not, see <http://www.gnu.org/licenses/>.
;

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
