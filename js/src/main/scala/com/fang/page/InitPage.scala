package com.fang.page
import com.fang.{Main, UserSession}
import com.fang.ajax.UserAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{document, window}
import com.fang.ImplicitConvert._

class InitPage extends Page{
  override def title(): String = "will changed"

  @dom override def onLoad(): Binding[Node] = {
    val url = Main.getHash(window.location.href)
    UserAPI.logStatus().map {
      case Ok(value) =>
        println(value.toString)
        if(Main.getHash(window.location.href) == url && Main.page.value == this){
          val page = Main.router.fromUrl(url).getOrElse(new Main.NotFound(url))
          Main.page.value = page
          document.title = page.title()
        }
      case Error(message, code) =>
        println("an invalid login is found")
        window.location.hash = "#login"
    }
    <h1 class="text-center">Loading</h1>
  }
}
