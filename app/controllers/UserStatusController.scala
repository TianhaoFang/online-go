package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Flow
import com.fang.{ErrorMessage, UserStatus}
import com.fang.UserStatus.{Idle, Playing, Waiting}
import models.{GamePlayDAO, GamePlayModel}
import play.api.mvc._
import util.MyActions.{MyAction, MyRequest}
import util.{UParser, ValidUser}
import webSocket._
import upickle.default.{read, write}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class UserStatusController @Inject()
(
  globalActors: GlobalActors,
  gamePlayDAO: GamePlayDAO,
  implicit val executionContext: ExecutionContext
) extends Controller {

  import globalActors._

  def validUser(userId: String): ActionBuilder[MyRequest] = MyAction andThen ValidUser(userId)

  def connectWebSocket(userId: String): WebSocket = WebSocket.acceptOrResult { requestHeader =>
    val request = new MyRequest(MyAction.getUserSession(requestHeader), Request(requestHeader, ""))
    if (!request.isValidUser(userId)) {
      ValidUser.genError("not authenticated").map(s => Left(s.get))
    } else {
      Future.successful(Right(Flow.fromProcessor(() =>
        new UserStatusWebSocket(this, globalActors, userId))))
    }
  }

  def queryStatus(userId: String): Future[UserStatus] = {
    Future.sequence(Seq(
      gamePlayDAO.queryRunningGame(userId).map(_.map { gp =>
        UserStatus.playing(userId, gp.id.toString)
      }),
      waitListActor.getUserStatus(userId).map(_.map { rule =>
        UserStatus.waiting(userId, rule)
      })
    )).map(_ reduce { (a1, a2) => a1.orElse(a2) })
      .map(_.getOrElse(UserStatus.idle(userId)))
  }

  def errorMessage(message: String): String = write(ErrorMessage(message))

  def leftPlayMethod: Future[Either[ErrorMessage, UserStatus]] =
    Future.successful(Left(ErrorMessage("could not set user to play mode")))

  def updateStatus(userId: String, userStatus: UserStatus): Future[Either[ErrorMessage, UserStatus]] = {
    import Future.{successful => instant}
    if (userStatus.userId != userId) return instant(Left(ErrorMessage("userId not same")))
    queryStatus(userId).flatMap {
      case Idle(_) => userStatus match {
        case Idle(_) => instant(Right(UserStatus.idle(userId)))
        case Waiting(_, rule) =>
          waitListActor.addToWaitList(rule, userId).map {
            case true => Right(UserStatus.waiting(userId, rule))
            case false => Left(ErrorMessage("failed to make user to waitlist"))
          }.recover {
            case exception: Exception => Left(ErrorMessage(exception.getMessage))
          }
        case _ =>
          leftPlayMethod
      }
      case Waiting(_, rule) => userStatus match {
        case Idle(_) => waitListActor.removeFromWaitList(userId).map {
          _ => Right(UserStatus.idle(userId))
        }
        case Waiting(_, oldRule) => if (oldRule == rule) {
          instant(Right(userStatus))
        } else {
          instant(Left(ErrorMessage("player is now waiting for another rule " + rule)))
        }
        case _ => leftPlayMethod
      }
      case Playing(_, gameId) =>
        instant(Left(ErrorMessage("player is now playing at " + gameId)))
    }
  }
}
