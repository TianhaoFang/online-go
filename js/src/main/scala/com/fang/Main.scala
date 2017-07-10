package com.fang

import com.fang
import com.fang.page.{InitPage, LoginPage, Page, RegisterPage}
import com.thoughtworks.binding.Binding.{BindingInstances, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.{HashChangeEvent, document, window}
import org.scalajs.dom.raw.{Event, Node}
import tinyrouter.Router

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExportTopLevel

object Main extends JSApp {

  import ImplicitConvert._
  import tinyrouter.TinyRouter.{static, dynamic, UrlExtractor}

  val router: Router[Page] = tinyrouter.Router[Page](
    dynamic[LoginPage](_ => s"login"){
      case url"login" => new LoginPage
    },
    dynamic[RegisterPage](_ => "register"){
      case url"register" => new RegisterPage
    }
  )

  var page: Var[Page] = Var(defaultPage)
  val feedback = new fang.page.Page.Feedback

  class NotFound(val path: String) extends Page {
    override def title(): String = "Not Found"

    @dom override def onLoad(): Binding[Node] = {
      <div>
        Not found path for
        {path}
      </div>
    }
  }

  def defaultPage: Page = { new InitPage }

  override def main(): Unit = {
    document.title = page.value.title()
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
