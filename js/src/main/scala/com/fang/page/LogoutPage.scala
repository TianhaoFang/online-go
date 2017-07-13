package com.fang.page
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._
import com.fang.ajax.UserAPI
import com.fang.data.GlobalValue
import org.scalajs.dom.window

class LogoutPage extends Page{
  override def title(): String = "logout"

  @dom override def onLoad(): Binding[Node] = {
    UserAPI.logout().foreach(s => {
      window.location.hash = "login"
      GlobalValue.updateUserSession()
    })
    <h1 class="text-center">Logout</h1>
  }
}
