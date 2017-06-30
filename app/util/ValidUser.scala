package util

import com.fang.ErrorMessage
import play.api.mvc.{ActionFilter, Result, Results}
import util.MyActions.MyRequest
import upickle.default.write

import scala.concurrent.Future

class ValidUser(val id: String) extends ActionFilter[MyRequest] {
  override protected def filter[A](request: MyRequest[A]): Future[Option[Result]] = {
    if (request.isValidUser(id)) {
      Future.successful(None)
    } else {
      ValidUser.genError("could not find user by id, or user not match")
    }
  }
}

object ValidUser {
  def apply(id: => String):ValidUser = {
    new ValidUser(id)
  }

  def genError(message: String): Future[Some[Result]] = Future.successful(Some(
    Results.Unauthorized(write(ErrorMessage(message)))
  ))

  object adminOnly extends ActionFilter[MyRequest] {
    override protected def filter[A](request: MyRequest[A]): Future[Option[Result]] = {
      if(request.isAdmin){
        Future.successful(None)
      }else{
        genError("should access with admin")
      }
    }
  }
}
