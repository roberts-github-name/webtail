package webtail.nettydemo

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import org.apache.logging.log4j.scala.Logging

object NettyWsEcho extends Logging {
  private val PORT: Int = 8080

  def main(args: Array[String]): Unit = {
    logger.trace("BOOT")

    // NioEventLoopGroup
    //    MultithreadEventLoopGroup // Netty: multithreaded executor that handles multiple channels; distributes the workload of channels (~runnables) across a multithreaded executor (?)
    //        MultithreadEventExecutorGroup // Netty, exec. grp. that runs tasks in multiple threads it directly manages
    //            AbstractEventExecutorGroup //  Netty, partial implementation of EventExecutorGroup to reduce boilerplate for typical use cases (?)
    //                EventExecutorGroup ----------<-------  // Netty, iface for a group of executors with clean shutdown for all of them (aggregate pattern)
    //                    ScheduledExecutorService        |  // Java, iface for exec. svc. where tasks can be run now, or at some point later
    //                        ExecutorService             |  // Java, iface for an executor that can be shut down
    //                            Executor                ^  // Java, iface for running Runnables. Decouples when/how to run from what to run (command pattern)
    //                            AutoClosable            |
    //                    Iterable<EventExecutor>         |
    //                      EventExecutorGroup  ---->-----
    //        EventLoopGroup // Netty, special executor service that deals in Netty Channels instead of runnables (?)
    //            ScheduledExecutorService
    //            Iterable<EventExecutor>
    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup(1)

    try {
      val b = new ServerBootstrap() // EZ create server that we will bind to a port and will accept and maintain connections
      b
        .group(bossGroup, workerGroup) // tell the server to have bossGroup handle establishing incoming connections, and childGroup will maintain them
        .channel(classOf[NioServerSocketChannel]) // to make new channels, server will no-args construct a NioServerSocketChannel.
        // IMPORTANT (?): we use NioEventLoopGroup, so we need NioServerSocketChannels: Nio <-> Nio
//        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new EchoChannelInitializer)
        .option(ChannelOption.SO_BACKLOG, Integer.valueOf(128)) // kernel limit on number of pending connections, to avoid overflow

      val ch = b.bind(PORT).sync().channel() // .sync() waits for future to complete, in this case the binding

      logger.trace(s"Started HTTP->WebSocket server at ${ch.localAddress()}")

      ch
        .closeFuture() // a Future that, when completed, means "the channel is closed now"
        .sync()
    }
    // gracefully: will report isShuttingDown=True until shut down, will accept tasks and reset
    // a shutdown countdown on new tasks. Default countdown is 2 seconds.
    // I suppose the idea is if you're submitting tasks and something shuts down in the middle,
    // you probably want to wait a bit and retry shutting down. So schedule a count for later
    // (default 2s)
    workerGroup.shutdownGracefully()
    bossGroup.shutdownGracefully()
  }
}
