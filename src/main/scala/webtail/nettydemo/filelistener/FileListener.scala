package webtail.nettydemo.filelistener

import java.io.File
import java.time.Instant

/**
 * Visitor/listener pattern: entities extending this can be notified of changes to a file
 * when they occur.
 *
 * Listeners are registered with a watcher.
 */
trait FileListener {
  def onCreate(file: File, ctx: WatcherContext): Unit

  def onAccess(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit

  def onModify(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit

  def onGrow(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit

  def onShrink(file: File, oldLength: Long, newLength: Long, ctx: WatcherContext): Unit

  def onDelete(file: File, ctx: WatcherContext): Unit
}
