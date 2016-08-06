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

(ns com.github.wolf480pl.mafia.player
    (require [com.github.wolf480pl.mafia.util :refer (counter)]
             [com.github.wolf480pl.mafia.room :refer (claimNick)]))

(defprotocol Player
    (sendMsg [player, msg] "Sends a message to the player")
    (joinRoom [player, room] "Makes the player join the specified room")
    (roomLeft [player, room] "Called whenever the player leaves a room")
    (getDefaultCommand [player] "Returns the player's default command")
    (getRoomRegistry [player] "Returns the player's room registry")
    (getRoom [player] "Returns the player's room"))

(defrecord PlayerImpl [id, sendfn, state, roomReg]
    Player (sendMsg [player, msg]
               (sendfn msg))
           (joinRoom [player, room]
               (let [nick (format "Player%d" id)]
                   (swap! state assoc :room room) ;FIXME: We don't know yet if claimNick will succeed
                   (claimNick room nick player)))
           (roomLeft [player, room]
               (swap! state dissoc :room))
           (getDefaultCommand [player] (:defaultCmd @state))
           (getRoomRegistry [player] roomReg)
           (getRoom [player] (:room @state)))

(defn makePlayer [id, sendfn, roomRegistry]
    (->PlayerImpl id sendfn (atom {:defaultCmd "say", :room nil}) roomRegistry))

(defprotocol PlayerRegistry
    "A registry of players"
    (newPlayer [registry, sendfn] "Creates and registers a new player")
    (getPlayer [registry, id] "Returns the player with the specified ID, if any"))

(defn playerRegistry [roomRegistry]
    (let [idCtr (counter)
          registry (agent {})]
        (reify PlayerRegistry
            (newPlayer [_, sendfn]
                (let [id (idCtr)
                      player (makePlayer id sendfn roomRegistry)]
                    (send registry assoc id player)
                    player))
            (getPlayer [_, id]
                (get @registry id)))))
