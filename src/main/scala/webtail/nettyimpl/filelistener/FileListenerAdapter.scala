package webtail.nettyimpl.filelistener

import java.io.File
import java.time.Instant

/**
 * Netty default adapter:
 *    class Foo: interface with abstract methods
 *    class FooAdapter: interface with the canonical "do nothing" implementations
 */
class FileListenerAdapter extends FileListener {
  override def onCreate(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = ()

  override def onAccess(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = ()

  override def onModify(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = ()

  override def onGrow(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = ()

  override def onShrink(file: File, oldLength: Long, newLength: Long, ctx: WatcherContext): Unit = ()

  override def onDelete(file: File, ctx: WatcherContext): Unit = ()
}