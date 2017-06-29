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
      Future.successful(Some(
        Results.Unauthorized(write(ErrorMessage("could not find user by id")))
      ))
    }
  }
}

object ValidUser {
  def apply(id: => String):ValidUser = {
    new ValidUser(id)
  }
}
