package webtail.nettyimpl.filelistener.casper

import org.apache.logging.log4j.scala.Logging

import java.io.File
import java.lang.ref.{Reference, WeakReference}
import scala.collection.mutable

/**
 * The runnable registered with a cleaner to remove stale watcher entries
 * after a watcher becomes phantom reachable.
 *
 * PhantomWatchService holds only weak references to watchers. When a watcher stops re-registering
 * itself with its executor, it first becomes weakly reachable. The JVM then atomically clears
 * all the weak references since they're not registered with a reference queue. Then the JVM detects
 * that it's now only phantom reachable. Some time later, the JVM puts the phantom reference in
 * the cleaner into a queue, and the cleaner's daemon thread(s) dequeues this runnable and runs it.
 *
 * NOTE: [[V]] is intended to be a [[Reference]] subtype, otherwise this class will be
 *       a strong reference to [[v]] and it will never get cleaned up. This is a common
 *       source of bugs in async cleanup operations like this.
 */
case class RemoveEntry[K, V](k: K, v: V, from: mutable.Map[K, V]) extends Runnable with Logging {
  override def run(): Unit = {
    from.synchronized {
      // NOTE: V is intended to be a non-strong reference, otherwise the GC will never clear it
      //       and this cleanup action will never fire
      if (from.get(k).contains(v)) {
        from.remove(k)
        logger.trace(s"Cleaned up $k -> $v")
      } else {
        // only remove k->v pair if the current value matches v, otherwise we
        // could remove a different watcher that isn't done yet
        logger.trace(s"Map does not have an entry for $k, or its entry doesn't match $v")
      }
    }
  }
}