package webtail.nettyimpl

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import webtail.NettyUtils.StringOps

/**
 * Webtail protocol:
 *   clients send messages matching "subscribe: /path/to/file"
 *
 *   server responds with "success: offset 12435"
 */
class WebtailHandler(svc: NotificationService) extends SimpleChannelInboundHandler[TextWebSocketFrame] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame): Unit = {
    msg.text() match {
      case WebtailHandler.subscribe(path) => svc.register(path, ctx.channel())

      case other =>
        val rtxt = f"Expected subscription: use \"subscribe: /path/to/file.txt\"\nReceived this: $other"
        ctx.writeAndFlush(rtxt.toTextFrame)
    }
  }
}

object WebtailHandler {
  final val subscribe = "subscribe: (.+)".r
}