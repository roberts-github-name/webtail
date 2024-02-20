package webtail

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketFrame}
import io.netty.handler.codec.http.{DefaultFullHttpResponse, HttpResponseStatus, HttpVersion}
import io.netty.util.CharsetUtil

object NettyUtils {
  implicit class ByteArrayOps(arr: Array[Byte]) {
    def unpooled: ByteBuf = Unpooled.wrappedBuffer(arr)
  }

  implicit class StringOps(s: String) {
    def utf: Array[Byte] = utf8

    def utf8: Array[Byte] = s.getBytes(CharsetUtil.UTF_8)

    /** Converts this string to a HTTP response with [[s]] as the body and the given status code. */
    def toHttpResponse(code: HttpResponseStatus): DefaultFullHttpResponse = {
      new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, code, s.utf.unpooled)
    }

    def toTextFrame: WebSocketFrame = new TextWebSocketFrame(s)
  }
}
