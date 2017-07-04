package com.fang.ajax

import com.fang.{ErrorMessage, UserStatus}
import org.scalajs.dom._
import upickle.default.read

object UserStatusAPI {
  type ReceiveType = Either[ErrorMessage, UserStatus]

  def wsProtocol: String =
    if (window.location.protocol.startsWith("https")) "wss://" else "ws://"

  def wsUrl(userId: String): String = {
    s"$wsProtocol/user/$userId/status"
  }

  abstract class UserStatusSocket(val userId: String) {
    val socket = new WebSocket(wsUrl(userId))
    socket.onerror = (event: ErrorEvent) => onError(event.message)
    socket.onclose = onClose(_)
    socket.onopen = onOpen(_)
    socket.onmessage = onMessage(_)

    private def onMessage(event: MessageEvent) = {
      val data: String = event.data.asInstanceOf[String]
      try {
        val transformed: ReceiveType = read[ReceiveType](data)
        onReceive(transformed)
      } catch {
        case error: Exception => onError(error.getMessage)
      }
    }

    def close(): Unit = socket.close()

    def onClose(event: CloseEvent): Unit

    def onError(message: String): Unit

    def onOpen(event: Event): Unit

    def onReceive(data: ReceiveType): Unit
  }

  abstract class TypedUserStatusSocket(userId: String) extends UserStatusSocket(userId) {
    override def onReceive(data: ReceiveType): Unit = data match {
      case Left(ErrorMessage(str)) => onErrorMessage(str)
      case Right(userStatus) => onData(userStatus)
    }

    def onData(userStatus: UserStatus)

    def onErrorMessage(message: String)
  }

}
