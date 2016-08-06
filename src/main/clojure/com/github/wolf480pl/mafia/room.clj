(ns com.github.wolf480pl.mafia.room)

(defprotocol Room
    (claimNick [room, nick, player] "Claims the nick in the room for the player. If the player already has a nick in the room, and claiming succeeds, the old nick is released.")
    (broadcast [room, message] "Sends the message to all players in the room")
    (getNick [room, player] "Returns the player's nick in the room"))

(defprotocol RoomRegistry
    "A registry of rooms"
    (getRoom [registry, name] "Returns the room with the specified name. Creates one, if it doesn't exist."))
