//package webtail.springdemo
//
//import org.springframework.context.annotation.{Bean, Configuration}
//import org.springframework.web.socket.WebSocketHandler
//import org.springframework.web.socket.config.annotation.{EnableWebSocket, WebSocketConfigurer, WebSocketHandlerRegistry}
//import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean
//
//@Configuration // FIXME ????
//@EnableWebSocket // FIXME ????
//class EchoConfig extends WebSocketConfigurer {
//
//  override def registerWebSocketHandlers(registry: WebSocketHandlerRegistry): Unit = {
//    registry
//      .addHandler(echoHandler(), "/echo")
//      .setAllowedOrigins("*")
//  }
//
//  @Bean // FIXME ????
//  def echoHandler(): WebSocketHandler = {
//    new EchoHandler()
//  }
//
//  @Bean
//  def createWebSocketContainer(): ServletServerContainerFactoryBean = {
//    val container = new ServletServerContainerFactoryBean()
//    container.setMaxTextMessageBufferSize(8192)
//    container.setMaxBinaryMessageBufferSize(8192)
//    container
//  }
//}
