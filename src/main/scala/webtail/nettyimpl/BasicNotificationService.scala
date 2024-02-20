package webtail.nettyimpl
import io.netty.channel.Channel
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.EventExecutor

import java.io.File
import java.nio.file.{FileSystems, LinkOption, Path, StandardWatchEventKinds, WatchEvent, WatchKey, WatchService}
import java.util.concurrent.TimeUnit
import scala.collection.mutable

class BasicNotificationService(executor: EventExecutor) extends NotificationService {

  val ws = FileSystems.getDefault.newWatchService()
  val p = Path.of("./foo.txt").toRealPath()

  val listeners = mutable.Map.empty[Path, (Long, DefaultChannelGroup)]

  val t = new Thread {
    override def run(): Unit = {
      while (true) {
        val k = ws.take()

        k.pollEvents()
        k match {
          case StandardWatchEventKinds.OVERFLOW => ()
          case StandardWatchEventKinds.ENTRY_MODIFY =>
            

          case StandardWatchEventKinds.ENTRY_DELETE => ???
        }
      }

    }
  }

  /** Tells the service that any appending to `path` should be sent to `channel` */
  override def register(path: String, channel: Channel): Boolean = {
    val cf = new File(path).getCanonicalFile
    if (!cf.exists()) return false

//    watchers.synchronized {
//      watchers.get(cf) match {
//        case None =>
//
//      }
//    }

    true
  }

  override def remove(path: String, channel: Channel): Boolean = ???
}
