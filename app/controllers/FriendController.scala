package controllers

import javax.inject.Inject
import javax.inject.Singleton

import com.fang.{ErrorMessage, FriendModel}
import models.FriendDAO
import play.api.mvc.{Action, ActionBuilder, AnyContent, Controller}
import util.MyActions.MyAction
import util.{MyActions, ValidUser}
import upickle.default.write

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FriendController @Inject()(friendDAO: FriendDAO, implicit val executionContext: ExecutionContext) extends Controller{
  def validAction(userId: String): ActionBuilder[MyActions.MyRequest] = MyAction andThen ValidUser(userId)

  def getAllFriends(userId: String): Action[AnyContent] = validAction(userId).async{ implicit request =>
    friendDAO.findFriend(userId).map(seq => Ok(write(seq)))
  }

  def getFriendById(userId: String, friendId: String): Action[AnyContent] = validAction(userId).async { implicit request =>
    if(userId == friendId) Future.successful(BadRequest(write(ErrorMessage("the userId should not be equal to friendId"))))
    else {
      friendDAO.findFriend(userId, friendId).map{
        case None => NotFound(write(ErrorMessage("not find such friend, or not accepting invent")))
        case Some(friend) => Ok(write(friend))
      }
    }
  }

  def getInvited(userId: String): Action[AnyContent] = validAction(userId).async{ implicit request =>
    friendDAO.findInvented(userId).map(seq => Ok(write(seq)))
  }

  def getInvitedById(userId: String, friendId: String): Action[AnyContent] = validAction(userId).async { implicit request =>
    if(userId == friendId) Future.successful(BadRequest(write(ErrorMessage("the userId should not be equal to friendId"))))
    else {
      friendDAO.findInvented(userId, friendId).map{
        case None => NotFound(write(ErrorMessage("not find such invention")))
        case Some(friend) => Ok(write(friend))
      }
    }
  }

  def deleteRelation(userId: String, friendId: String): Action[AnyContent] = validAction(userId).async{ implicit request =>
    if(userId == friendId) Future.successful(BadRequest(write(ErrorMessage("the userId should not be equal to friendId"))))
    else {
      friendDAO.deleteRelation(userId, friendId).map{
        case 0 => NotFound(write(ErrorMessage("not find the invention or friend")))
        case _ => Gone(write(ErrorMessage("successful delete the relation")))
      }
    }
  }

  def makeInvite(userId: String, friendId: String): Action[AnyContent] = validAction(userId).async{ implicit request =>
    if(userId == friendId) Future.successful(BadRequest(write(ErrorMessage("the userId should not be equal to friendId"))))
    else{
      friendDAO.createRelation(userId, friendId).map(_ > 0)
        .map(b => Ok(write(b)))
    }
  }

  def acceptInvitation(userId: String, friendId: String): Action[AnyContent] = validAction(userId).async{ implicit request =>
    friendDAO.findInvented(userId, friendId).map(_.filter(_.user2 == userId)).map{
      case None => NotFound(write(ErrorMessage("no such invention or you invent others")))
      case Some(friendModel) =>
        friendDAO.updateRelation(friendModel.copy(accepted = true))
        Ok(write(ErrorMessage("accepted")))
    }
  }
}
