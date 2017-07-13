package util

import java.time.Instant

import com.fang.UserSession
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtUpickle}
import play.api.mvc._
import upickle.default

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object MyActions {
  val jwtAlgorithm = JwtAlgorithm.HS256
  val secretKey = "secretKey"
  var expireInSecond = 300L
  val cookieName = "jwt"

  def decode(token: String): Option[UserSession] =
    JwtUpickle.decode(token, secretKey, Seq(jwtAlgorithm))
      .flatMap(claim => Try(default.read[UserSession](claim.content))) match {
      case Success(userSession) => Some(userSession)
      case Failure(_) => None
    }

  def encode(userSession: UserSession): String = {
    val claim = JwtClaim(
      content = default.write[UserSession](userSession),
      expiration = Some(Instant.now.plusSeconds(expireInSecond).getEpochSecond)
    )
    JwtUpickle.encode(claim, secretKey, jwtAlgorithm)
  }

  class MyRequest[A](var user: Option[UserSession], request: Request[A])
    extends WrappedRequest[A](request) {
    def isValidUser(id: String): Boolean = user match {
      case None => false
      case Some(userSession) => userSession.id == id || userSession.role == "admin"
    }

    def isAdmin: Boolean = user.isDefined && (user.get.role == "admin")

    def isLogin: Boolean = user.isDefined
  }

  object MyAction extends ActionBuilder[MyRequest] {
    def getUserSession(request: RequestHeader): Option[UserSession] =
      request.headers.get("Authorization").map(_.replace("Bearer", ""))
        .orElse(request.cookies.get(cookieName).map(_.value))
        .flatMap(decode)

    override def invokeBlock[A](request: Request[A], block: (MyRequest[A]) => Future[Result]): Future[Result] = {
      val userSession: Option[UserSession] = getUserSession(request)
      val myRequest = new MyRequest[A](userSession, request)
      block(myRequest)
        .map(result => result.as("application/json"))
        .map(result => {
          myRequest.user match {
            case Some(session) => result.withCookies(Cookie(cookieName, encode(session)))
            case None => result.discardingCookies(DiscardingCookie(cookieName))
          }
        })
    }
  }

}
