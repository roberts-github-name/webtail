/**
 * REMOVED
 *
 * Why?
 *
 * At least on Windows, the WatchService doesn't actually respond to changes in file size.
 * It only seems to respond to changes in file metadata that occur when Windows and/or NTFS
 * decide to redetermine file size, which only happens when the size and seemingly other
 * metadata is requested.
 *
 * Instead of deploying hacks, I'm going to make my own polling based file thing.
 */


//package webtail.nettydemo.filelistener
//
//import com.sun.nio.file.SensitivityWatchEventModifier
//
//import java.nio.file.{FileSystems, Path, StandardWatchEventKinds, WatchEvent}
//import scala.jdk.CollectionConverters.CollectionHasAsScala
//
//// IDEA: have a watcher service. If someone says "hey watch this file for me"
////       then the service returns a watcher, or maybe has a listener that
////       does callbacks or whatever. Then the service abstracts how we get watchers
////       and such
//
//class FileWatcher(val p: Path) extends Thread {
//  private val fs = FileSystems.getDefault
//  private val ws = fs.newWatchService()
//  p.getParent.register(
//    ws,
//    Array[WatchEvent.Kind[_]](
//      StandardWatchEventKinds.ENTRY_DELETE,
//      StandardWatchEventKinds.ENTRY_MODIFY,
//      StandardWatchEventKinds.ENTRY_CREATE))
//
//  override def run(): Unit = {
//    try {
//      while (true) {
//        println("Taking an event...")
//        val k = ws.take()
//        println("  Took event(s).")
//        k.pollEvents().asScala.foreach { e => e.kind match {
//          case StandardWatchEventKinds.ENTRY_MODIFY =>
//            println("    MODIFIED:" + e.context() + s" ${e.count()} time(s)")
//          case other =>
//            println(s"    OTHER KIND: kind is ${e.kind}, e.context=${e.context}")
//        }}
//        k.reset() // if we don't call this, no events of its kind will be returned
//      }
//    } catch {
//      case _: InterruptedException => println("Watcher: interrupted.")
//    }
//  }
//}