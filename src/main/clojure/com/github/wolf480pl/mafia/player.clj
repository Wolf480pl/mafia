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
