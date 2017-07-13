package webSocket

import com.fang.UserStatus.{QueryUS, UpdateUS}
import com.fang.{ErrorMessage, UserStatus}
import controllers.UserStatusController
import upickle.default.{read, write}

import scala.concurrent.ExecutionContext

class UserStatusWebSocket
(
  userStatusController: UserStatusController,
  globalActors: GlobalActors,
  userId: String
)(
  implicit val executionContext: ExecutionContext
) extends WebSocketProcessor[String, String] {

  import globalActors._

  type ReplyType = Either[ErrorMessage, UserStatus]

  def sendResult(userStatus: UserStatus): Unit =
    sendMessage(write[ReplyType](Right(userStatus)))

  def sendError(errorMessage: ErrorMessage): Unit =
    sendMessage(write[ReplyType](Left(errorMessage)))

  def sendError(message: String): Unit = sendError(ErrorMessage(message))

  override def onConnected(): Unit = {
    userStatusBoarder.addOutput(userId, this)
    userStatusBoarder.boardCast(userId, { webSocket =>
      if (webSocket != this) {
        webSocket.sendMessage(write[ReplyType](
          Left(ErrorMessage("another user client connected to user status"))
        ))
        webSocket.closeClient()
      }
    })
    userStatusController.queryStatus(userId).foreach{ status =>
      sendResult(status)
    }
  }

  override def onCloseClient(): Unit = {
    // on client close, first break out the connection
    userStatusBoarder.removeOutput(userId, this)
    // and then remove all the output
    waitListActor.removeFromWaitList(userId)
  }

  override def onReceive(message: String): Unit = {
    try {
      read[UserStatus.USWebSocket](message) match {
        case QueryUS() =>
          userStatusController.queryStatus(userId).foreach(sendResult)
        case UpdateUS(userStatus) =>
          userStatusController.updateStatus(userId, userStatus).foreach {
            case Left(errorMessage) => sendError(errorMessage)
            case Right(newStatus) => sendResult(newStatus)
          }
      }
    } catch {
      case exception: Exception => sendError(exception.getMessage)
    }
  }
}
