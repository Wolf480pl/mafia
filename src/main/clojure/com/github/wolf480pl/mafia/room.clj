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

(ns com.github.wolf480pl.mafia.room)

(defprotocol Room
    (claimNick [room, nick, player] "Claims the nick in the room for the player. If the player already has a nick in the room, and claiming succeeds, the old nick is released.")
    (leave [room, player-or-nick] "Makes the provided player-or-nick leave the room, releasing their nick.")
    (broadcast [room, message] "Sends the message to all players in the room")
    (getNick [room, player] "Returns the player's nick in the room")
    (isOwner [room, player] "Returns whether the specified player owns the room"))

(defprotocol RoomRegistry
    "A registry of rooms"
    (getRoom [registry, name]
             [registry, name, creator] "Returns the room with the specified name. Creates one, if it doesn't exist, and the creator is specified"))
