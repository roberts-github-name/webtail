package webtail.nettyimpl

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import org.apache.commons.io.input.ReversedLinesFileReader
import webtail.utils.NettyUtils.StringOps
import webtail.nettyimpl.filelistener.{FileListener, FileWatcherService}

import java.io.{File, FileReader}
import scala.collection.mutable
import scala.jdk.CollectionConverters.IteratorHasAsScala

/**
 * Webtail protocol:
 *   clients send messages matching "subscribe: /path/to/file"
 *
 *   server responds with "success: offset 12435"
 */
class WebtailHandler(svc: FileWatcherService) extends SimpleChannelInboundHandler[TextWebSocketFrame] {

  private val listeners = mutable.Map.empty[File, FileListener]

  override def channelRead0(ctx: ChannelHandlerContext, msg: TextWebSocketFrame): Unit = {
    msg.text() match {
      case WebtailHandler.tailf(fname) =>
        val f = new File(fname).getCanonicalFile
        if (f.exists()) updateListener(f, new TailLilFListener(ctx))
        else sendError(ctx, s"Cannot tail -f: $fname does not exist.")

      case WebtailHandler.tailF(fname) =>
        val f = new File(fname).getCanonicalFile
        updateListener(f, new TailBigFListener(ctx))

      case WebtailHandler.tail(fname) =>
        var reader: ReversedLinesFileReader = null
        try {
          reader = ReversedLinesFileReader.builder().setFile(fname).get()
          val lines = reader.readLines(10).reversed().iterator().asScala.mkString("\n")
          ctx.writeAndFlush((s"Last 10 lines of $fname:\n" + lines).toTextFrame)
        } catch {
          case e: Exception => sendError(ctx, s"Could not tail $fname: ${e.toString}")
        } finally {
          if (reader != null) reader.close()
        }

      case WebtailHandler.unsubscribe(fname) =>
        val f = new File(fname).getCanonicalFile
        updateListener(f, null)

      case other =>
        sendError(ctx, s"Unrecognized request: $other. Try `tail [-f|-F] <file>` or `unsubscribe <file>`")
    }
  }

  override def channelUnregistered(ctx: ChannelHandlerContext): Unit = {
    listeners.foreach { case (f, l) => svc.remove(f, l) }
    ctx.fireChannelUnregistered()
  }

  private def sendError(ctx: ChannelHandlerContext, msg: String): Unit = {
    ctx.writeAndFlush(msg.toTextFrame)
  }

  private def updateListener(f: File, listener: FileListener): Unit = {
    listeners.get(f).foreach(l => svc.remove(f, l))
    if (listener != null) {
      listeners(f) = listener
      svc.watch(f, listener)
    }
  }
}

object WebtailHandler {
  final val subscribe = "subscribe: (.+)".r

  final val tailf = """tail -f (.+)""".r
  final val tailF = """tail -F (.+)""".r
  final val tail = """tail (.+)""".r
  final val unsubscribe = """unsubscribe (.+)""".r
}