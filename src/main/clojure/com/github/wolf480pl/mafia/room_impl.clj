(ns com.github.wolf480pl.mafia.room-impl
    (:require [com.github.wolf480pl.mafia.util :refer (putIfAbsent)]
              [com.github.wolf480pl.mafia.player :refer (sendMsg)]
              [com.github.wolf480pl.mafia.room :refer :all]))

(defn onClaimNick [room, oldNick, nick, player, reason]
    (if-not reason
        (do
          (broadcast room (if-not oldNick
                             (format "%s joined %s" nick (.name room))
                             (format "%s changed nick to %s" oldNick nick)))
          (sendMsg player (format "Joined %s as %s" (.name room) nick)))
        (sendMsg player (format "Can't join %s: %s" (.name room) reason))))

(defrecord RoomImpl [name, state]
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
          (broadcast [room, msg]
              ;(map (fn [[nick player]] (sendMsg player msg)) (first (deref (.state room))))
              (doseq [[nick, player] (first @state)]
                  (sendMsg player msg)))
          (getNick [room, player]
              (let [[_, pl2nick, _] @state]
                  (pl2nick player))))

(defn makeRoom [name]
    (->RoomImpl name (agent [{}, {}, nil])))

(defn roomRegistry []
    (let [reg (atom {})]
        (reify RoomRegistry
            (getRoom [_, name]
                (putIfAbsent reg name (makeRoom name))))))
