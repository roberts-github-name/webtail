package webtail.nettydemo.filelistener

import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileSystem, FileSystems, Files, NoSuchFileException, Paths}
import java.time.Instant

sealed trait FileState

object FileState {
  case class Exists(lastAccessed: Instant, lastModified: Instant, length: Long) extends FileState

  case object DoesNotExist extends FileState

  def of(file: File): FileState = {
    try {
      val attrs = Files.readAttributes(file.toPath, classOf[BasicFileAttributes])
      Exists(attrs.lastAccessTime().toInstant, attrs.lastModifiedTime().toInstant, attrs.size())
    } catch {
      case _: NoSuchFileException => DoesNotExist
    }
  }
}