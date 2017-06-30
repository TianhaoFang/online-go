package controllers

import javax.inject.{Inject, Singleton}

import com.fang.{ErrorMessage, UserModel}
import models.{SecureHash, UserDAO}
import play.api.mvc.{Action, AnyContent, Controller, Results}
import util.MyActions.MyAction
import com.fang.UserModel._
import upickle.default.{read, write}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.{UParser, ValidUser}

import scala.concurrent.Future

@Singleton
class UserController @Inject()(userDAO: UserDAO) extends Controller {
  def getUser(id: String): Action[AnyContent] = MyAction.async { implicit request =>
    userDAO.getUserByName(id).map {
      case None => NotFound(write(ErrorMessage(s"not found for user $id")))
      case Some(user) => {
        if (request.isValidUser(id)) {
          Ok(write(user.noPassword))
        } else {
          Ok(write(user.toView))
        }
      }
    }
  }

  def hash(value: String) = MyAction { implicit request =>
    val result = SecureHash.encode(value)
    Ok(write((result, result.length)))
  }

  def createUser(): Action[UserModel] = MyAction.async(UParser(read[UserModel])) { implicit request =>
    val user = request.body
    if (user == null || user.username.trim == "") {
      Future.successful(BadRequest(write(ErrorMessage("username should not be null"))))
    } else {
      userDAO.getUserByName(user.username).flatMap {
        case Some(_) => Future.successful(BadRequest(write(ErrorMessage("user already exists"))))
        case None =>
          val hashedUser = user.copy(password = SecureHash.encode(user.password))
          userDAO.insertUser(hashedUser).map { columns =>
            Created(write(ErrorMessage("create success")))
          }
      }
    }
  }

  def updateUser(userId: String): Action[NoPassword] = (MyAction andThen ValidUser(userId))
    .async(UParser(read[UserModel.NoPassword])){ implicit request =>
      val user = request.body
      userDAO.updateUser(userId, user).map {
        case 0 => NotFound(write(ErrorMessage("not found user")))
        case _ => Ok(user)
      }
  }
}
