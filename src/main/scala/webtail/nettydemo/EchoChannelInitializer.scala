package webtail.nettydemo

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpDecoderConfig, HttpObjectAggregator, HttpServerCodec}
import org.apache.logging.log4j.scala.Logging

/**
 * Used by a server bootstrap to configure newly created channels, such as for TCP connections.
 *
 * The pipeline is
 *   HttpServerCodec for bytes <--> HttpRequest
 */
class EchoChannelInitializer extends ChannelInitializer[SocketChannel] with Logging {
  println("EchoChannelInitializer: instantiated")

  override def initChannel(ch: SocketChannel): Unit = {
    println("EchoChannelInitializer.initChannel: SIGNS OF LIFE")
    logger.trace("Initializing channel")

    ch
      .pipeline()
      .addLast(new HttpServerCodec(new HttpDecoderConfig().setValidateHeaders(true))) // bytes <--> HttpRequest, both directions
      .addLast(new HttpObjectAggregator(1024)) // aggregates client-side and netty-side HTTP request chunks
      .addLast(new UpgradeHttpToWsHandler(new WsEchoHandler)) // upgrades to WS connection, then replaces itself with WsEchoHandler
  }
}