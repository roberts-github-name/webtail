package webtail.nettydemo

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.{ChannelHandler, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory
import io.netty.handler.codec.http.{DefaultFullHttpResponse, DefaultHttpResponse, HttpRequest, HttpResponse, HttpResponseStatus, HttpVersion, QueryStringDecoder}
import io.netty.util.CharsetUtil
import org.apache.logging.log4j.scala.Logging
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import webtail.NettyUtils.{ByteArrayOps, StringOps}

import java.net.http.HttpHeaders
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IteratorHasAsScala, MapHasAsScala}
/**
 * Takes [[HttpRequest]]s and handles valid WebSocket upgrade requests by replacing
 * this in the pipeline with [[wsHandler]].
 *
 * Logs and error and closes the connection is ANYTHING is amiss about the upgrade request.
 *
 * @param wsHandler a [[ChannelHandler]] that can handle WebSocket requests (specifically [[WebSocketFrame]])
 */
class UpgradeHttpToWsHandler(wsHandler: ChannelHandler) extends ChannelInboundHandlerAdapter with Logging{
  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    if (!msg.isInstanceOf[HttpRequest]) {
      // this can happen if the client or netty applies chunking
      //     clients: can chunk HTTP request, gross overkill for a simple upgrade request
      //     Netty: HttpServerHandler will chunk large messages over a (configurable?) size threshold
      logger.error(s"channelRead: received msg of type ${msg.getClass.getSimpleName} != HttpRequest: $msg")
      throw new RuntimeException(s"Unsupported message type: msg=$msg")
    }

    val req = msg.asInstanceOf[HttpRequest]
    val headers = req.headers()

    val valid =
      req.protocolVersion().compareTo(HttpVersion.HTTP_1_1) >= 0 &&
        headers.containsValue("Connection", "Upgrade", true) &&
        headers.containsValue("Upgrade", "WebSocket", true)

    if (!valid) {
      logger.error("Received HTTP request that is not a WebSocket upgrade request.")
      ctx.writeAndFlush(
        "This is a WebSocket server. Reconnect with a valid WebSocket upgrade handshake."
          .toHttpResponse(HttpResponseStatus.BAD_REQUEST))
      ctx.close()
      return
    }

    // factory: in this case, represents a curried function of type HttpRequest => handshaker.
    //     the websocket URL is only needed for an ancient prototype version of websockets so we leave it null;
    //     the only version in the wild is v13
    val wsHsF = new WebSocketServerHandshakerFactory(null, null, false)

    // ONLY looks at the websocket version in the header (value of "sec-websocket-version")
    val hs = wsHsF.newHandshaker(req)

    if (hs == null) {
      logger.error(s"Failed to create a handshaker for $req: unsupported version. Sending unsupported version response.")
      WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
      ctx.close()
      return
    }

    // Throws if it determines the request isn't valid after all
    // You'd think Netty would throw earlier when making the handshaker since it can tell it would be doomed...
    // but it doesn't!
    // The handshaker just looks at protocol versions. This just looks at headers afaik.
    hs.handshake(ctx.channel(), req)

    logger.trace(s"Handshake successful. Replacing ${this.getClass.getSimpleName} with given WebSocket handler $wsHandler of type ${wsHandler.getClass.getSimpleName}")
    ctx.pipeline().replace(this, s"upgraded_to_${wsHandler.getClass.getSimpleName}", wsHandler)
  }
}