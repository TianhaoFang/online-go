package com.fang.ajax

import com.fang.UserStatus.USWebSocket
import com.fang.data.AjaxResult
import com.fang.data.AjaxResult.AjaxResult
import com.fang.{ErrorMessage, UserStatus}
import org.scalajs.dom._
import org.scalajs.dom.ext.Ajax
import upickle.default.{read, write}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object UserStatusAPI {
  type ReceiveType = Either[ErrorMessage, UserStatus]

  def wsProtocol: String =
    if (window.location.protocol.startsWith("https")) "wss://" else "ws://"

  def wsUrl(relPath: String): String = {
    wsProtocol + window.location.host + relPath
  }

  private def userWsPath(userId: String): String = wsUrl(s"/user/$userId/status-ws")

  def getStatus(userId: String): Future[AjaxResult[UserStatus]] =
    Ajax.get(s"/user/$userId/status")
    .map(AjaxResult.mapToResult(read[UserStatus]))
    .recover(AjaxResult.recovery)

  abstract class UserStatusSocket(val userId: String) extends WSConnection[ReceiveType, USWebSocket](userWsPath(userId)) {
    override def decode(input: String): ReceiveType = {
      read[ReceiveType](input)
    }

    override def encode(output: USWebSocket): String = write[USWebSocket](output)
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
