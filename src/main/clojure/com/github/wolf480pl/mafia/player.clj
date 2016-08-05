(ns com.github.wolf480pl.mafia.player
    (require [com.github.wolf480pl.mafia.util :refer :all]))

(defrecord Player [id, sendfn])

(defn sendMsg [player, msg]
    ((.sendfn player) msg))

(defprotocol PlayerRegistry
    "A registry of players"
    (newPlayer [registry, sendfn] "Creates and registers a new player")
    (getPlayer [registry, id] "Returns the player with the specified ID, if any"))

(defn playerRegistry []
    (let [idCtr (counter)
          registry (agent {})]
        (reify PlayerRegistry
            (newPlayer [_, sendfn]
                (let [id (idCtr)
                      player (->Player id sendfn)]
                    (send registry assoc id player)
                    player))
            (getPlayer [_, id]
                (get @registry id)))))
