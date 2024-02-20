package webtail.nettydemo.filelistener

import java.io.{BufferedOutputStream, File, FileOutputStream, FileWriter, PrintWriter}
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.nio.file.{FileSystems, Files, Path}
import java.time.{LocalTime, ZoneId, ZonedDateTime}
import scala.util.Random

/**
 * Weird Java stuff regarding WatchService
 *   > ENTRY_MODIFIED fires for changes to file metadata such as last accessed and last modified time
 *   > if the file is being written to, AND NOTHING ELSE (see below), then you do NOT get ENTRY_MODIFIED events
 *   > if the file is NOT being written to, and you change focus to IntelliJ or right-click the file, nothing happens
 *   > if the file IS being written to, then the FIRST of the following triggers an ENTRY_MODIFIED event
 *     > right-clicking the file in Explorer
 *     > moving focus back to IntelliJ
 *
 * My theory:
 *   > the watcher uses part of the Windows API to watch for events related to files, a lot more
 *     efficient than polling
 *   > the API notifies watchers if the metadata or contents of the file change
 *   > but the API does NOT monitor the underlying filesystem for changes; this is how we can
 *     flush data to the file without the API "noticing"
 *   > certain actions access metadata: right-clicking in Explorer, refocusing on IntelliJ
 *     (which I assume changes the project view as IntelliJ resyncs files)
 *   > when that data is accessed, Windows checks if it's up to date. It gets the size of the
 *     file and realizes "nope, not accurate." Then sets the size of the file in metadata (?)
 *     and that triggers the Windows API to notify listeners about file changed events.
 *
 * Support: Windows does NOT automatically update file sizes in explorer.
 * It remains e.g. 277K for at least a couple minutes until you right-click, then refresh Explorer
 * then the file size changes.
 */
object Demo {

  implicit class FileTimeMethods(ft: FileTime) {
    def toLocalTime: LocalTime = LocalTime.ofInstant(ft.toInstant, ZoneId.systemDefault())
  }

  def main(args: Array[String]): Unit = {
    val file = new File("./foo")
    if (file.exists()) file.delete()
    file.deleteOnExit()

    val w = new FileWatcher(file.toPath)
    w.start()

    println("Started watcher. Sleeping.")
    Thread.sleep(5000)

    println("Creating file.")
//    val out = new PrintWriter(new FileOutputStream(file))
    val out = new FileWriter(file)

    {
      val t = new Thread {
        override def run(): Unit = {
          while (true) {
            Thread.sleep(5000)
//            val before = "Before getting attributes: " + LocalTime.now
//            val a = Files.readAttributes(file.toPath, classOf[BasicFileAttributes])
//            println(before + s".    $file: size=${a.size}, creationTime=${a.creationTime.toLocalTime}, lastAccessTime=${a.lastAccessTime.toLocalTime}, lastModifiedTime=${a.lastModifiedTime.toLocalTime}, fileKey=${a.fileKey}.    After append: " + LocalTime.now)
//            println(s"File size: ${file.length}")
          }
        }
      }
      t.setDaemon(true)
      t.start()
    }

    try {
      (1 to 3000).foreach { _ =>
        Thread.sleep(3000)
        out.write(s"A line of output: ${Random.nextString(1000)}" + System.lineSeparator())
        out.flush()
        println(s"Wrote a line at ${LocalTime.now}")
//        out.append("asdf")
//        file.length() // this is side effecting!!
//        println(s"Modified file at ${ZonedDateTime.now}, size is ${file.length}")
      }
    } finally {
      out.close()
    }

    Thread.sleep(5000)
    println("Deleting file")
    file.delete()

    Thread.sleep(5000)
    w.interrupt()
    println("Done.")
  }
}
