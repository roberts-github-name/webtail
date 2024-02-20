package webtail.nettyimpl

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

/**
 * Initializes a channel pipeline as follows:
 *
 * IN     HTTP deser    (optional) de-chunk HTTP     websocket boilerplate     WebtailHandler
 * OUT
 *
 * @param chunk iff true, HttpObjectAggregator is added to the pipeline to handle chunking.
 *              Enable this for robustness at the cost of overhead when chunking is never used.
 * @param debug iff true, all messages after HTTP ser/deser and HTTP aggregation are logged at trace level
 */
class WebtailInitializer(chunk: Boolean, debug: Boolean) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    // see https://github.com/netty/netty/blob/4.1/codec-http/src/main/java/io/netty/handler/codec/http/websocketx/WebSocketServerProtocolHandler.java
    // for a bare-bones example of websockets

    val p = ch.pipeline()

    /** HTTP boilerplate: encode/decode (server codec is duplex), chunk support */
    p.addLast(new HttpServerCodec)

    if (chunk) p.addLast(new HttpObjectAggregator(Short.MaxValue)) // 32 KiB - 1 B

    /** debug */
    if (debug) p.addLast(new LoggingHandler(LogLevel.TRACE)) // DEBUG ONLY

    /** websocket handling */
    // NOTE: this impl may be a bit less efficient because it reponds to ping frames
    //       and has an extra handler in the chain that is just a no-op for the text messages we want
    p
      .addLast(new WebSocketServerProtocolHandler("", null, false, 1_000_000, false, true, 5000))
//      .addLast(new WebtailHandler)
  }
}
