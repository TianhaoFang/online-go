package controllers

import javax.inject._

import com.fang.{Test, UserSession}
import play.api.mvc._
import upickle.default._
import util.MyActions.MyAction
import util.UParser

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action { implicit request =>
    println(Test.text)
    println(upickle.default.write(Test.User("cccddd", 16)))
    Ok(views.html.index())
  }

  def test: Action[Test.User] = Action(UParser(read[Test.User])) { implicit request =>
    Ok(request.body.toString)
  }

  def getSession: Action[AnyContent] = MyAction{ implicit request =>
    Ok(request.user.toString)
  }

  def postSession: Action[UserSession] = MyAction(UParser(read[UserSession])){ implicit request =>
    request.user = Some(request.body)
    Ok(request.user.toString)
  }

  val ppp:BodyParser[Test.User] = UParser(read[Test.User])
}
