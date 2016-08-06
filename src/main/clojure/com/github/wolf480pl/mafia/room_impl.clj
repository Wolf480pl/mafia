(ns com.github.wolf480pl.mafia.room-impl
    (:require [com.github.wolf480pl.mafia.util :refer (putIfAbsent)]
              [com.github.wolf480pl.mafia.player :refer (sendMsg roomLeft)]
              [com.github.wolf480pl.mafia.room :refer :all]))

(defn onClaimNick [room, oldNick, nick, player, reason]
    (if-not reason
        (do
          (broadcast room (if-not oldNick
                             (format "%s joined %s" nick (.name room))
                             (format "%s changed nick to %s" oldNick nick)))
          (if-not oldNick (sendMsg player (format "Joined %s as %s" (.name room) nick))))
        (sendMsg player (format "Can't join %s: %s" (.name room) reason))))

(defn onLeave [room, nick, player]
    (broadcast room (format "%s left %s" nick (.name room)))
    (roomLeft player room))

(defrecord RoomImpl [name, state, owner]
    Room (claimNick [room, nick, player]
             (some-> (agent-error state) (throw))
             (send (.state room)
                   (fn [[nick2pl, pl2nick, game]]
                       (let [reason (if game (:started)
                                        (if (contains? nick2pl nick) :taken nil))
                             oldNick (pl2nick player)]
                           (onClaimNick room oldNick nick player reason)
                           (if reason
                               [nick2pl, pl2nick, game]
                               [(-> (if oldNick (dissoc nick2pl oldNick) nick2pl) (assoc nick player)),
                                (assoc pl2nick player nick),
                                game])))))
         (leave [room, player-or-nick]
             (send state
                   (fn [[nick2pl, pl2nick, game]]
                       (let [[player, nick] (if (string? player-or-nick)
                                                [(nick2pl player-or-nick), player-or-nick]
                                                [player-or-nick, (pl2nick player-or-nick)])]
                           (if-not (and player nick)
                               [nick2pl, pl2nick, game]
                               (do
                                 (onLeave room nick player)
                                 [(dissoc nick2pl nick), (dissoc pl2nick player), game]))))))
         (broadcast [room, msg]
             ;(map (fn [[nick player]] (sendMsg player msg)) (first (deref (.state room))))
             (doseq [[nick, player] (first @state)]
                 (sendMsg player msg)))
         (getNick [room, player]
             (let [[_, pl2nick, _] @state]
                 (pl2nick player)))
         (isOwner [room, player]
             (identical? player owner)))

(defn makeRoom [name, owner]
    (->RoomImpl name (agent [{}, {}, nil]) owner))

(defn roomRegistry []
    (let [reg (atom {})]
        (reify RoomRegistry
            (getRoom [_, name]
                (@reg name))
            (getRoom [_, name, creator]
                (putIfAbsent reg name (makeRoom name creator))))))
