package webtail.nettydemo

import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, CloseWebSocketFrame, PingWebSocketFrame, PongWebSocketFrame, TextWebSocketFrame, WebSocketFrame}
import io.netty.util.CharsetUtil
import org.apache.logging.log4j.scala.Logging

import java.nio.charset.Charset

class WsEchoHandler extends ChannelInboundHandlerAdapter with Logging {

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    msg match {
      case b: BinaryWebSocketFrame =>
        val buf = b.content()
        val bytes = new Array[Byte](buf.readableBytes())
        buf.readBytes(bytes)
        logger.info(s"Received binary, printing as UTF-8: ${new String(bytes, CharsetUtil.UTF_8)}")
        ctx.channel().writeAndFlush(b)

      case t: TextWebSocketFrame =>
        logger.info(s"Received text: ${t.text()}")
        ctx.channel.writeAndFlush(t)

      case ping: PingWebSocketFrame =>
        logger.info("Received ping frame")
        ctx.channel.writeAndFlush(new PongWebSocketFrame(null))

      case pong: PongWebSocketFrame =>
        logger.info(s"Received pong frame")

      case c: CloseWebSocketFrame =>
        logger.info("Received close frame, closing connection.")
        ctx.close()

      case _ =>
        logger.error(s"Received msg of type ${msg.getClass.getSimpleName} that is not supported: $msg")
        ctx.close()
    }

  }

}
