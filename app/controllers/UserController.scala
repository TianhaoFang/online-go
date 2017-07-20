package controllers

import javax.inject.{Inject, Singleton}

import com.fang._
import models.{SecureHash, UserDAO}
import play.api.mvc.{Action, AnyContent, Controller, Results}
import util.MyActions.MyAction
import com.fang.UserModel._
import upickle.default.{read, write}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.ValidUser.adminOnly
import util.{UParser, ValidUser}

import scala.concurrent.Future

@Singleton
class UserController @Inject()(userDAO: UserDAO) extends Controller {
  def getUser(id: String): Action[AnyContent] = MyAction.async { implicit request =>
    userDAO.getUserByName(id).map {
      case None => NotFound(write(ErrorMessage(s"not found for user $id")))
      case Some(user) =>
        if (request.isValidUser(id)) {
          Ok(write(user.noPassword))
        } else {
          Ok(write(user.toView))
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
    .async(UParser(read[UserModel.NoPassword])) { implicit request =>
      val user = request.body
      userDAO.updateUser(userId, user).map {
        case 0 => NotFound(write(ErrorMessage("not found user")))
        case _ => Ok(write(user))
      }
    }

  def updatePassword(userId: String): Action[Password] = (MyAction andThen ValidUser(userId))
    .async(UParser(read[Password])) { implicit request =>
      import Future.{successful => suss}
      val password = request.body
      userDAO.getUserByName(userId).flatMap {
        case None => suss(NotFound(write(ErrorMessage(s"not found for user $userId"))))
        case Some(user) =>
          val oldPassword = SecureHash.encode(password.oldPassword)
          if (user.password != oldPassword && !request.isAdmin)
            suss(BadRequest(write(ErrorMessage("password is incorrect"))))
          else {
            userDAO.updatePassword(userId, SecureHash.encode(password.password))
              .map {
                case 1 => Ok(write(ErrorMessage("success changed password")))
                case _ => NotFound(write(ErrorMessage(s"not found for user $userId")))
              }
          }
      }
    }

  private val invalidLogin = BadRequest(write(ErrorMessage("invalid username or password")))

  def login(): Action[LoginRequest] = MyAction.async(UParser(read[LoginRequest])) { implicit request =>
    val body = request.body
    userDAO.getUserByName(body.username).map {
      case None => invalidLogin
      case Some(user) =>
        if (user.password == SecureHash.encode(body.password)) {
          val result = UserSession(body.username)
          request.user = Some(result)
          Ok(write(result))
        } else {
          invalidLogin
        }
    }
  }

  def logout() = MyAction { implicit request =>
    request.user = None
    Ok(write(ErrorMessage("logout")))
  }

  def getLoginStatus: Action[AnyContent] = MyAction { implicit request =>
    request.user match {
      case None => Unauthorized(write(ErrorMessage("not login")))
      case Some(userSession) => Ok(write(userSession))
    }
  }

  def adminLogin(): Action[LoginRequest] = MyAction.async(UParser(read[LoginRequest])) { implicit request =>
    val username = request.body.username
    val password = request.body.password
    userDAO.getAdmin(username).map {
      case None => invalidLogin
      case Some(admin) =>
        if (admin.password == SecureHash.encode(password)) {
          val result = UserSession(username, "admin")
          request.user = Some(result)
          Ok(write(result))
        } else {
          invalidLogin
        }
    }
  }

  def updateAdminPassword(): Action[Password] = (MyAction andThen adminOnly)
    .async(UParser(read[Password])) { implicit request =>
      import Future.{successful => suss}
      val username = request.user.get.id
      userDAO.getAdmin(username).flatMap {
        case None => suss(NotFound(write(ErrorMessage(s"not found for admin $username"))))
        case Some(admin) =>
          if (admin.password == SecureHash.encode(request.body.oldPassword)) {
            userDAO.updateAdmin(LoginRequest(username, SecureHash.encode(request.body.password)))
              .map {
                case 1 => Ok(write(ErrorMessage("update success")))
                case _ => BadRequest(write(ErrorMessage("server error")))
              }
          } else {
            suss(BadRequest(write(ErrorMessage("password is incorrect"))))
          }
      }
    }

  def searchUser(userName: String): Action[AnyContent] = Action.async{ implicit request =>
    userDAO.searchUser(userName).map(seq => Ok(write[Seq[String]](seq)))
  }

  def indexPage(): Action[AnyContent] = Action{ implicit request =>
    Redirect("index.html")
  }
}
