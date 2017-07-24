package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Flow
import com.fang.{ErrorMessage, UserStatus}
import com.fang.UserStatus.{Idle, InviteStatus, Invited, Playing, Waiting}
import com.fang.game.GameStatus
import models.{FriendDAO, GamePlayDAO, GamePlayModel, UserDAO}
import play.api.mvc._
import util.MyActions.{MyAction, MyRequest}
import util.{UParser, ValidUser}
import webSocket._
import upickle.default.{read, write}
import webSocket.WaitListActor.MatchingResult

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class UserStatusController @Inject()
(
  globalActors: GlobalActors,
  gamePlayDAO: GamePlayDAO,
  friendDAO: FriendDAO,
  userDAO: UserDAO,
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

  def getState(userId: String): Action[AnyContent] = Action.async{ implicit request =>
    userDAO.getUserByName(userId).flatMap {
      case None =>
        Future.successful(NotFound(errorMessage("not find user")))
      case Some(_) =>
        queryStatus(userId).map{ userStatus =>
          Ok(write[UserStatus](userStatus))
        }
    }
  }

  def queryStatus(userId: String): Future[UserStatus] = {
    Future.sequence(Seq(
      gamePlayDAO.queryRunningGame(userId).map(_.map { gp =>
        UserStatus.playing(userId, gp.id.toString)
      }),
      waitListActor.getUserStatus(userId).map(_.map { rule =>
        UserStatus.waiting(userId, rule)
      }),
      inviteGameActor.queryInvite(userId).map(_.map {invite =>
        UserStatus.invited(userId, invite)
      })
    )).map(_ reduce { (a1, a2) => a1.orElse(a2) })
      .map(_.getOrElse(UserStatus.idle(userId)))
  }

  def errorMessage(message: String): String = write(ErrorMessage(message))

  def leftPlayMethod: Future[Either[ErrorMessage, UserStatus]] =
    Future.successful(Left(ErrorMessage("could not set user to play mode and invite mode")))

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
        case Invited(_, InviteStatus(user1, user2, rule)) =>
          if(userId != user1 || user1 == user2) instant(Left(ErrorMessage("could only invite others")))
          else{
            friendDAO.findFriend(user1, user2).flatMap{
              case None => instant(Left(ErrorMessage("no such friend:" + user2)))
              case Some(_) =>
                queryStatus(user2).flatMap {
                  case Playing(_, gameId) => instant(Left(ErrorMessage("the other user is playing at " + gameId)))
                  case Waiting(_, waitRule) => instant(Left(ErrorMessage("the other player is waiting for game:" + waitRule)))
                  case Idle(_) =>
                    inviteGameActor.makeInvite(userId, user2, rule).map {
                      case Left(error) =>
                        Left(ErrorMessage(error))
                      case Right(_) =>
                        val inviteStatus = InviteStatus(user1, user2, rule)
                        userStatusBoarder.boardCast(user2, _.sendMessage(write[Either[ErrorMessage, UserStatus]](
                          Right(UserStatus.invited(user2, inviteStatus))
                        )))
                        Right(UserStatus.invited(userId, inviteStatus))
                    }
                  case _ => instant(Left(ErrorMessage("the other player is not able to receive invite")))
                }

            }

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
      case inv@Invited(_, InviteStatus(user1, user2, rule)) => userStatus match {
        case Idle(_) =>
          inviteGameActor.removePendingInvite(user1, user2).map{_ =>
            val other = if(user1 == userId) user2 else user1
            userStatusBoarder.boardCast(other, {_.sendMessage(write[Either[ErrorMessage, UserStatus]](
              Right(UserStatus.idle(other))
            ))})
            Right(UserStatus.idle(userId))
          }
        case Playing(_, _) =>
          if(user1 == userId) instant(Left(ErrorMessage("only invented people could determine weather to play game")))
          else if(user2 != userId) instant(Left(ErrorMessage("could not accept the invitation by other identity")))
          else{
            GamePlayController.startNewGame(MatchingResult(rule, user1, user2), globalActors, gamePlayDAO)
            instant(Right(inv))
          }
        case _ => leftPlayMethod
      }
    }
  }
}
