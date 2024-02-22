package webtail.nettydemo.filelistener

import java.io.File

/**
 * Core entity that users interact with to listen to events for a file.
 *
 * This mostly serves as GoF facade to shield users from the complications
 * of concurrency when dealing with a multithreaded polling setup.
 */
abstract class FileWatcherService {
  def watch(file: File, listener: FileListener): Unit

  def remove(file: File, listener: FileListener): Unit
}
