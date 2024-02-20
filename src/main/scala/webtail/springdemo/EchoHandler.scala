//package webtail.springdemo
//
//import org.springframework.web.socket.{AbstractWebSocketMessage, BinaryMessage, CloseStatus, TextMessage, WebSocketHandler, WebSocketMessage, WebSocketSession}
//
//class EchoHandler extends WebSocketHandler {
//  override def afterConnectionEstablished(session: WebSocketSession): Unit = {
//    session.sendMessage(new TextMessage("Connection established."))
//  }
//
//  override def handleMessage(session: WebSocketSession, message: WebSocketMessage[_]): Unit = {
//    session.sendMessage(message)
//  }
//
//  override def handleTransportError(session: WebSocketSession, exception: Throwable): Unit = ???
//
//  override def afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus): Unit = ???
//
//  override def supportsPartialMessages(): Boolean = false
//}
