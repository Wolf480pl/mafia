(ns com.github.wolf480pl.mafia.netty
    (:require [clojure.string :as str]
              [com.github.wolf480pl.mafia.player :refer :all]
              [com.github.wolf480pl.mafia.room :refer (leave)]
              [com.github.wolf480pl.mafia.room-impl :refer (roomRegistry)]
              [com.github.wolf480pl.mafia.command :refer (->Command regStandardCommands dispatchCmd)]))

(import '(io.netty.bootstrap ServerBootstrap)
        '(io.netty.channel.nio NioEventLoopGroup)
        '(io.netty.channel ChannelOption ChannelInitializer SimpleChannelInboundHandler ChannelHandler ChannelFutureListener)
        '(io.netty.channel.socket.nio NioServerSocketChannel)
        '(io.netty.handler.codec LineBasedFrameDecoder MessageToMessageDecoder)
        '(io.netty.handler.codec.string StringDecoder StringEncoder)
        '(io.netty.handler.timeout IdleStateHandler)
        '(java.net InetSocketAddress)
        '(java.nio.charset Charset))

(def MAX_LINE_LENGTH 512)
(def IDLE_TIMEOUT 30)
(def CHARSET (Charset/forName "UTF-8"))


(defn commandDecoder []
    (proxy [MessageToMessageDecoder] []
        (decode [ctx, msg, out]
            (when (seq msg)
                (let [splitMsg (str/split msg #" ")]
                    (.add out (->Command (first splitMsg) (vec (rest splitMsg)))))))))

(defn slashCommandDecoder []
    (proxy [MessageToMessageDecoder] []
        (decode [ctx, msg, out]
            (when (seq msg)
                (let [splitMsg (str/split msg #" ")
                      cmd (first splitMsg)
                      cmd (if (str/starts-with? cmd "/") (.substring cmd 1))
                      args (if cmd (rest splitMsg) splitMsg)]
                    (.add out (->Command cmd (vec args))))))))

(comment
(defn handler []
    (proxy [SimpleChannelInboundHandler] []
        (channelRead0 [ctx, msg]
             (println msg)
             (.writeAndFlush (.channel ctx) (format "got: %s\n" msg))
        )))
)


(defn handler [playerReg, commandReg]
    (let [ourPlayer (atom nil)]
        (proxy [SimpleChannelInboundHandler] []
            (channelRead0 [ctx, msg]
                (println (.name msg) (.args msg))
                (.writeAndFlush (.channel ctx) (format "cmd: %s args: %s\n" (.name msg) (.args msg)))
                (dispatchCmd commandReg @ourPlayer msg)
            )
            (channelActive [ctx]
                (let [player (newPlayer playerReg
                                        (fn [msg] (.writeAndFlush (.channel ctx) (format "%s\n" msg))))]
                    (sendMsg player (format "Hello Player%d" (.id player)))
                    (reset! ourPlayer player)))
            (channelInactive [ctx]
                (let [player @ourPlayer]
                    (some-> (getRoom player) (leave player)))))))

(defn initializer [handlerFactory]
    (proxy [ChannelInitializer] []
        (initChannel [ch]
            (let [pipeline (.pipeline ch)]
                ; --- inbound ---
                (.addLast pipeline (into-array ChannelHandler [
                                    (LineBasedFrameDecoder. MAX_LINE_LENGTH)
                                    (StringDecoder. CHARSET)
                                    (IdleStateHandler. IDLE_TIMEOUT, 0, 0)
                                    (slashCommandDecoder)
                                    (handlerFactory)
                                   ]))
                ; --- outbound ---
                (.addLast pipeline (into-array ChannelHandler [(StringEncoder. CHARSET)]))
             ))))

(defprotocol NettyServer
    "A netty server of the game"
    (stop [server] "Stop the server"))


(defn start [address]
    (let [commandReg (regStandardCommands {})
          playerReg (playerRegistry (roomRegistry))
          bossGroup (NioEventLoopGroup. 1)
          workerGroup (NioEventLoopGroup.)
          bootstrap (doto (ServerBootstrap.)
                        (.group bossGroup, workerGroup)
                        (.channel NioServerSocketChannel)
                        (.childHandler (initializer #(handler playerReg commandReg)))
                        (.childOption ChannelOption/TCP_NODELAY true)
                        (.childOption ChannelOption/SO_KEEPALIVE true))
          future (.bind bootstrap address)
          channel (.channel future)]
        [future (reify NettyServer
                    (stop [_]
                        (-> channel
                            (.close)
                            (.addListener (reify ChannelFutureListener
                                              (operationComplete [_, _]
                                                  (.shutdownGracefully workerGroup)
                                                  (.shutdownGracefully bossGroup)))))))])) 
