package webtail.nettyimpl

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.concurrent.{DefaultEventExecutorGroup, GlobalEventExecutor}
import org.apache.logging.log4j.scala.Logging
import webtail.nettyimpl.filelistener.casper.PhantomWatchService

object StartServer extends Logging {
  final val PORT = 8080
  final val BOSS_THREADS = 1
  final val CHILD_THREADS = 4
  final val NOTIFY_THREADS = 4

//  final val channels = new DefaultChannelGroup()

  def main(args: Array[String]): Unit = {
    val bossGroup = new NioEventLoopGroup(BOSS_THREADS)
    val childGroup = new NioEventLoopGroup(CHILD_THREADS)
    val fileWatcherService = new PhantomWatchService

    try {
      val b = new ServerBootstrap()

      b
        .group(bossGroup, childGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.TRACE))
        .childHandler(new WebtailInitializer(chunk = false, debug = true, fileWatcherService))
        .option(ChannelOption.SO_BACKLOG, Integer.valueOf(10))

      val ch = b.bind(PORT).sync().channel()

      logger.info(s"Server bound to ${ch.localAddress()}")

      ch.closeFuture().sync()
    } catch {
      case t: Throwable => logger.error(t)
    } finally {
      bossGroup.shutdownGracefully()
      childGroup.shutdownGracefully()
    }
  }

}
