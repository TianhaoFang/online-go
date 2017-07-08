package com.fang.ajax

import com.fang.{ErrorMessage, UserStatus}
import org.scalajs.dom._
import upickle.default.read

object UserStatusAPI {
  type ReceiveType = Either[ErrorMessage, UserStatus]

  def wsProtocol: String =
    if (window.location.protocol.startsWith("https")) "wss://" else "ws://"

  def wsUrl(relPath: String): String = {
    wsProtocol + window.location.host + relPath
  }

  private def userWsPath(userId: String): String = wsUrl(s"/user/$userId/status")

  abstract class UserStatusSocket(val userId: String) extends WSConnection[ReceiveType, String](userWsPath(userId)){
    override def decode(input: String): ReceiveType = {
      read[ReceiveType](input)
    }

    override def encode(output: String): String = output
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
