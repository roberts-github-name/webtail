package webtail.nettyimpl.filelistener.casper

import io.netty.util.CharsetUtil
import webtail.nettyimpl.filelistener.{FileListener, WatcherContext}

import java.io.{File, FileWriter, PrintWriter}
import java.lang.ref.{Cleaner, PhantomReference, Reference, ReferenceQueue, WeakReference}
import java.nio.file.Files
import java.time.Instant
import scala.util.Random


object Demo {
  def main(args: Array[String]): Unit = {
    val watchService = new PhantomWatchService

    val file = new File("./foo")
    file.delete()
    file.deleteOnExit()

    val l = new FileListener {
      override def onCreate(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = println(s"$file created. Contents: ${new String(newBytes)}")
      override def onAccess(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = println(s"$file accessed at $thisTime")
      override def onModify(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = println(s"$file modified at $thisTime")
      override def onGrow(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = println(s"$file had this added: ${new String(newBytes, CharsetUtil.UTF_8)}")
      override def onShrink(file: File, oldLength: Long, newLength: Long, ctx: WatcherContext): Unit = println(s"$file shrank from $oldLength to $newLength")
      override def onDelete(file: File, ctx: WatcherContext): Unit = {
        println(s"$file deleted")
        ctx.unsubscribe()
      }
    }

    val l2 = new FileListener {
      override def onCreate(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = ()
      override def onAccess(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = ()
      override def onModify(file: File, prevTime: Instant, thisTime: Instant, ctx: WatcherContext): Unit = ()
      override def onGrow(file: File, newBytes: Array[Byte], ctx: WatcherContext): Unit = ()
      override def onShrink(file: File, oldLength: Long, newLength: Long, ctx: WatcherContext): Unit = ()
      override def onDelete(file: File, ctx: WatcherContext): Unit = {
        ctx.unsubscribe()
      }
    }

    println("          REGISTERING")
    watchService.watch(file, l)
    watchService.watch(file, l2)

    Thread.sleep(5L)

    println("          CREATING")
    var writer = new PrintWriter(new FileWriter(file, true))

    Thread.sleep(5000L)
    println("          WRITING")
    (0 to 10).foreach { i =>
      writer.println("Here's a line.")
      writer.flush()
      Thread.sleep(500)
    }
    writer.close()

    Thread.sleep(5000)
    println("          SHRINKING")
    writer = new PrintWriter(new FileWriter(file, false))
    writer.println("Short.")
    writer.flush()

    Thread.sleep(5000)
    println("          DELETING")
    writer.flush()
    writer.close()
    Files.delete(file.toPath)

    Thread.sleep(5000)
    println("          GC")
    System.gc()
    System.gc()

    Thread.sleep(5000)
    System.gc()
    Thread.sleep(1000)
    println("          DONE")
    watchService.stop()
  }

}
