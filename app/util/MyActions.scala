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
  var expireInSecond = 10L
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
  }

  object MyAction extends ActionBuilder[MyRequest] {
    override def invokeBlock[A](request: Request[A], block: (MyRequest[A]) => Future[Result]): Future[Result] = {
      val userSession: Option[UserSession] =
        request.headers.get("Authorization").map(_.replace("Bearer", ""))
          .orElse(request.cookies.get(cookieName).map(_.value))
          .flatMap(decode)
      val myRequest = new MyRequest[A](userSession, request)
      block(myRequest).map(result => {
        myRequest.user match {
          case Some(session) => result.withCookies(Cookie(cookieName, encode(session)))
          case None => result.discardingCookies(DiscardingCookie(cookieName))
        }
      })
    }
  }
}
