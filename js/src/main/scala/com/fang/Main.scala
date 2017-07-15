package com.fang

import com.fang
import com.fang.data.GlobalValue
import com.fang.page._
import com.fang.page.game.DemoGameScene
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Node, UIEvent}
import org.scalajs.dom.{HashChangeEvent, document, window}
import tinyrouter.Router

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExportTopLevel

object Main extends JSApp {

  import ImplicitConvert._
  import tinyrouter.TinyRouter.{UrlExtractor, dynamic}

  val router: Router[Page] = tinyrouter.Router[Page](
    dynamic[LoginPage](_ => s"login"){
      case url"login" => new LoginPage
    },
    dynamic[RegisterPage](_ => "register"){
      case url"register" => new RegisterPage
    },
    dynamic[ProfilePage](p => s"user/${p.userId}"){
      case url"user/$userId" => new ProfilePage(userId)
    },
    dynamic[LogoutPage](_ => "logout"){
      case url"logout" => new LogoutPage()
    },
    dynamic[UserStatusPage](s => s"user/${s.userId}/status"){
      case url"user/$userId/status" => new UserStatusPage(userId)
    },
    dynamic[DemoGameScene](s => s"game/${s.gameId}"){
      case url"game/$gameId" => new DemoGameScene(gameId)
    },
    dynamic[FriendPage](s => s"user/${s.userId}/friends"){
      case url"user/$userId/friends" => new FriendPage(userId)
    }
  )

  var page: Var[Page] = Var(defaultPage)
  val feedback = new fang.page.Page.Feedback

  def defaultPage: Page = { new InitPage }

  override def main(): Unit = {
    // document.title = page.value.title()
    dom.render(document.getElementById("app"), render)
    window.onhashchange = (e: HashChangeEvent) => {
      println(s"hash change: oldUrl: ${e.oldURL}, newUrl: ${e.newURL}")
      feedback.proceedLeave()
      page.value.onUnload(feedback)
      if (feedback.isCanceled) {
        window.history.replaceState(1, page.value.title(), e.oldURL)
      } else {
        val hashPath = getHash(e.newURL)
        val newPage = router.fromUrl(hashPath).getOrElse(new NotFound(hashPath))
        page.value = newPage
        document.title = newPage.title()
      }
    }

    window.onresize = (_:UIEvent) => {
      resetHW()
    }

    resetHW()
  }

  def resetHW(): Unit = {
    GlobalValue.windowHeight.value = window.innerHeight.toInt
    GlobalValue.windowWidth.value = window.innerWidth.toInt
  }

  @JSExportTopLevel("getHash")
  def getHash(url: String): String = {
    val index = url.indexOf('#')
    if (index < 0) ""
    else url.substring(index + 1)
  }

  @dom
  def render: Binding[Node] = {
    page.bind.onLoad().bind
  }
}
