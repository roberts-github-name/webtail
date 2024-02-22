package webtail.nettydemo.filelistener.casper

import webtail.nettydemo.filelistener.{FileListener, FileWatcherService, WatcherContext}

import java.io.File
import java.lang.ref.{Cleaner, ReferenceQueue, WeakReference}
import java.nio.file.WatchService
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.collection.mutable
import scala.ref.PhantomReference

class PhantomWatchService extends FileWatcherService {
  private val watchers = mutable.Map.empty[File, WeakReference[FileWatcher]]
  private val cleaner = Cleaner.create()
  private val exec = new ScheduledThreadPoolExecutor(1)
  exec.setExecuteExistingDelayedTasksAfterShutdownPolicy(false)

  def stop(): Unit = exec.shutdownNow()

  override def watch(file: File, listener: FileListener): Unit = {
    val f = file.getCanonicalFile

    watchers.synchronized {
      watchers.get(f) match {
        case Some(pe) if pe.get != null && pe.get().register(listener) => // .register() did the work
        case _ =>
          // could be None, or a weak reference with a nulled referent
          // create new watcher
          val watcher = new FileWatcher(f, exec)
          watcher.register(listener) // register first so watcher isn't empty and thus stop itself after the first run
          exec.schedule(watcher, 1, TimeUnit.SECONDS)

          val weakRef = new WeakReference(watcher)
          watchers(f) = weakRef
          cleaner.register(watcher, RemoveEntry(f, weakRef, watchers))
      }
    }
  }

  override def remove(file: File, listener: FileListener): Unit = {
    val f = file.getCanonicalFile
    watchers.synchronized {
      watchers
        .get(f)
        .flatMap(weakref => Option(weakref.get()))
        .foreach(_.remove(listener))
    }
  }
}
