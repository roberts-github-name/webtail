package webtail.nettyimpl.filelistener.casper

import org.apache.logging.log4j.scala.Logging
import webtail.nettyimpl.filelistener.FileState.{DoesNotExist, Exists}
import webtail.nettyimpl.filelistener.{FileListener, FileState, WatcherContext}
import webtail.nettyimpl.filelistener.FileState.{DoesNotExist, Exists}
import webtail.utils.FnUtils.TwoArgs

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Path}
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import scala.collection.mutable
import scala.concurrent.Future
import scala.ref.WeakReference

/**
 * Basic watcher that reschedules itself with an executor service as long as there
 * are listeners for the file it watches.
 *
 * Thread safe.
 *
 * In the event there are no listeners, this ceases to re-register.
 *
 * WARNING: listener callbacks are run synchronously. Avoid long-running code in implementations,
 *          such as by using a future.
 *
 *          TODO: consider allowing async by chaining futures; each listener is its own chain
 *
 * If all other references are weak or phantom, then Java's placement of phantom references
 * in a specified queue can be used as a roundabout callback for fine-grained cleanup.
 * [[PhantomWatchService]] leverages this to remove this watcher from its watchers map.
 *
 * Callbacks on all listeners are serialized such that, if a listener is registered
 * for only one file, all callbacks for that file are chained.
 *
 * TODO: ideally we'd put operations into chains of Futures for each listener
 *       but the problem I wound up with is this:
 *          > suppose we chain a bunch of updates to a slow listener, e.g. with 100 seconds of work
 *          > but the channel or other entity unsubscribes the listener after 20 seconds
 *          > then operations to commence after the 20 second point will continue to be called
 *            even though we unsubscribed
 *       Maybe the cardinal sin is "async unsubscribe;" perhaps an object should represent the
 *       state of said subscription
 *          Subscription
 *              Listener
 *              Future[WatcherContext]
 *              val done: False
 *
 *              add callback: sync on done, if not done then chain the future
 *              futures: chain on WatcherContext, AND check `done` to make sure we
 *              don't "zombie-continue"
 */
class FileWatcher(file: File, exec: ScheduledExecutorService) extends Runnable with Logging {
  private val listeners = mutable.Set.empty[FileListener] // subject to concurrent modifications

  // ctx and oldState are NOT intended to be concurrently modified; a watcher is intended to
  // only be registered with a single executor
  private val ctx = new WatcherContext // RI: prepped and ready; single instance minimizes GC burden
  private var oldState = FileState.of(file)
  private var done: Boolean = false

  def register(listener: FileListener): Boolean = {
    this.synchronized {
      if (done) false
      else {
        listeners += listener
        true
      }
    }
  }

  def remove(listener: FileListener): Unit = {
    this.synchronized {
      listeners -= listener
    }
  }

  override def run(): Unit = {
    val start = System.nanoTime()

    val newState = FileState.of(file)
    val notify = buildNotifyMethod(oldState, newState)

    this.synchronized {
      if (notify != null && listeners.nonEmpty) {
        lazy val toRemove = mutable.ArrayBuffer.empty[FileListener]
        listeners.foreach { l =>
          notify(l, ctx)
          if (ctx.unsubscribed) toRemove += l
          ctx.reset()
        }
        toRemove.foreach(listeners.remove)
        toRemove.foreach(l => println(s"Removed listener $l"))
      }

      if (listeners.nonEmpty) exec.schedule(this, 1, TimeUnit.SECONDS)
      else done = true
    }

    oldState = newState // we don't expect concurrent modifications so we set outside the synchronized block

    val nanos = System.nanoTime() - start
  }

  private def buildNotifyMethod(s1: FileState, s2: FileState): (FileListener, WatcherContext) => Unit = {
    (s1, s2) match {
      case (DoesNotExist, DoesNotExist) => null
      case (DoesNotExist, exists: Exists) => (l, c) => {
        var fis: FileInputStream = null
        try {
          fis = new FileInputStream(file)
          fis.getChannel.position(0L)
          val bytes = fis.readNBytes(exists.length.toInt)
          l.onCreate(file, bytes, c)
        } catch {
          case e: Exception => logger.error(s"$l: onCreate", e)
        } finally {
          if (fis != null) fis.close()
        }
      }
      case (_: Exists, DoesNotExist) => (l, c) => l.onDelete(file, c)
      case (o: Exists, n: Exists) =>
        val fns = mutable.ArrayBuffer.empty[(FileListener, WatcherContext) => Unit]
        if (n.lastAccessed != o.lastAccessed) fns += { (l, c) => l.onAccess(file, o.lastAccessed, n.lastAccessed, c) }
        if (n.lastModified != o.lastModified) fns += { (l, c) => l.onModify(file, o.lastModified, n.lastModified, c) }

        if (n.length > o.length) {
          var fis: FileInputStream = null
          try {
            fis = new FileInputStream(file) // inside try in case the file was deleted (race condition)
            fis.getChannel.position(o.length)
            val nbytes = (n.length - o.length).toInt
            val bytes = fis.readNBytes(nbytes)
            if (bytes.length != nbytes) println(s"Concurrent modification? File states said $nbytes added, but only read ${bytes.length}")
            fns += { (l, c) => l.onGrow(file, bytes, c) }
          } finally {
            if (fis != null) fis.close()
          }
        }
        else if (n.length < o.length) fns += { (l, c) => l.onShrink(file, o.length, n.length, c) }

        if (fns.isEmpty) null
        else fns.head.composeCond((_, c) => !c.unsubscribed, fns.tail.toSeq : _*)
    }
  }
}
