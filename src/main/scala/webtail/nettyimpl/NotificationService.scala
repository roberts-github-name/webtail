package webtail.nettyimpl

import io.netty.channel.Channel


trait NotificationService {
  /** Tells the service that any appending to `path` should be sent to `channel` */
  def register(path: String, channel: Channel): Boolean

  def remove(path: String, channel: Channel): Boolean
}
