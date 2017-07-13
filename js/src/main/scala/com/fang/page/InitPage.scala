package com.fang.page
import com.fang.{Main, UserSession}
import com.fang.ajax.UserAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.{document, window}
import com.fang.ImplicitConvert._
import com.fang.data.GlobalValue

class InitPage extends Page{
  override def title(): String = "will changed"

  @dom override def onLoad(): Binding[Node] = {
    val url = Main.getHash(window.location.href)
    GlobalValue.updateUserSession()
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
        if(window.location.hash != "#login"){
          window.location.hash = "#login"
        }else{
          Main.page.value = new LoginPage
          document.title = Main.page.value.title()
        }
    }
    <div>
      <h1 class="text-center">Loading</h1>
      <a href="#login" class="btn btn-default btn-block">Login</a>
    </div>
  }
}
