package webtail.nettyimpl

import io.netty.channel.ChannelHandlerContext
import io.netty.util.CharsetUtil
import webtail.utils.NettyUtils.StringOps
import webtail.nettyimpl.filelistener.{FileListener, FileListenerAdapter, WatcherContext}

import java.io.File
import java.time.Instant

class TailLilFListener(cctx: ChannelHandlerContext) extends FileListener {
  override def onCreate(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = {
    cctx.writeAndFlush(s"Closed: $file")
    ctx.unsubscribe()
  }

  override def onAccess(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = ()

  override def onModify(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = ()

  override def onGrow(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = {
    cctx.writeAndFlush((s"Grew: $file\n" + new String(newBytes, CharsetUtil.UTF_8)).toTextFrame)
  }

  override def onShrink(file: File, oldLength: Long, newLength: Long, ctx: WatcherContext): Unit = ()

  override def onDelete(file: File, ctx: WatcherContext): Unit = {
    cctx.writeAndFlush(s"Closed: $file")
    ctx.unsubscribe()
  }
}
