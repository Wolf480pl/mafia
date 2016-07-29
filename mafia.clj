(import '(io.netty.bootstrap ServerBootstrap)
        '(io.netty.channel.nio NioEventLoopGroup)
        '(io.netty.channel ChannelOption ChannelInitializer SimpleChannelInboundHandler ChannelHandler)
        '(io.netty.channel.socket.nio NioServerSocketChannel)
        '(io.netty.handler.codec LineBasedFrameDecoder)
        '(io.netty.handler.codec.string StringDecoder StringEncoder)
        '(io.netty.handler.timeout IdleStateHandler)
        '(java.net InetSocketAddress)
        '(java.nio.charset Charset))

(def MAX_LINE_LENGTH 512)
(def IDLE_TIMEOUT 30)
(def CHARSET (Charset/forName "UTF-8")) 

(defn handler []
    (proxy [SimpleChannelInboundHandler] []
        (channelRead0 [ctx, msg]
             (println msg)
             (.writeAndFlush (.channel ctx) (format "got: %s\n" msg))
        )))

(defn initializer []
    (proxy [ChannelInitializer] []
        (initChannel [ch]
            (let [pipeline (.pipeline ch)]
                ; --- inbound ---
                (.addLast pipeline (into-array ChannelHandler [
                                    (LineBasedFrameDecoder. MAX_LINE_LENGTH)
                                    (StringDecoder. CHARSET)
                                    (IdleStateHandler. IDLE_TIMEOUT, 0, 0)
                                    (handler)
                                   ]))
                ; --- outbound ---
                (.addLast pipeline (into-array ChannelHandler [(StringEncoder. CHARSET)]))
             ))))

(defn start [address]
    (def bossGroup (NioEventLoopGroup. 1))
    (def workerGroup (NioEventLoopGroup.))
    (let [bootstrap (ServerBootstrap.)]
        (.group bootstrap bossGroup, workerGroup)
        (.channel bootstrap NioServerSocketChannel)
        (.childHandler bootstrap (initializer))
        (.childOption bootstrap ChannelOption/TCP_NODELAY true)
        (.childOption bootstrap ChannelOption/SO_KEEPALIVE true)
        (let [future (.bind bootstrap address)]
            (def channel (.channel future))
            future))) 
