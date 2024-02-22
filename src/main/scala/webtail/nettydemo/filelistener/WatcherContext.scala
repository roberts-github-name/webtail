package webtail.nettydemo.filelistener

/**
 * Passed as a parameter to [[FileWatcherService]]s when they respond to file changes.
 *
 * This class allows listeners to change how they're notified by the watcher.
 *
 * To reduce GC pressure, the reset() method is the moral and NOT thread-safe equivalent
 * of creating a new context.
 *
 * Example:
 * {{{
 *   val ctx = new FileListenerContext // val, not var
 *   for (l <- listeners) {
 *     // val ctx = new FileListenerContext // no need to make a new one every iteration
 *     l.onChange(..., ctx)
 *     if (ctx.unsubscribed) ???
 *     ctx.clear() // clear after instead of using `new before`
 *   }
 * }}}
 */
class WatcherContext {
  private var _unsubscribe: Boolean = false

  /** Indicates that the watcher should forget about this listener forever. */
  def unsubscribe(): Unit = {
    _unsubscribe = true
  }

  def unsubscribed: Boolean = _unsubscribe

  def reset(): Unit = {
    _unsubscribe = false
  }
}
