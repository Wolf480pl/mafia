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
