package com.fang.page

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}
import com.fang.ImplicitConvert._
import com.fang.UserSession
import com.fang.ajax.UserAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.fang.data.GlobalValue
import com.fang.page.DomUtil.{bindInputValue, hideClassIf}
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.window

class LoginPage extends Page {
  override def title(): String = "login"

  val username = Var("")
  val password = Var("")
  @dom val allValid: Binding[Boolean] = username.bind.length > 0 && password.bind.length > 0
  val errorMessage:Var[Option[String]] = Var(None)

  @dom override def onLoad(): Binding[Node] = {
    <div class="container">
      <div class="jumbotron">
        <div class="container">
          <h1 class="">Online Go</h1>
          <p>An online platform to play gomoku game</p>
        </div>
      </div>
      <form name="loginForm" noValidate={true}>
        <div class="form-group">
          <label class="control-label">Username</label>
          <input type="text" name="username" class="form-control"
                 placeholder="Your name" oninput={bindInputValue(_: Event, username)}/>
        </div>
        <div class="form-group">
          <label class="control-label">Password</label>
          <input type="password" name="password" class="form-control"
                 placeholder="Password" oninput={bindInputValue(_: Event, password)}/>
        </div>
        <div class={hideClassIf("alert alert-danger", "hide", errorMessage.bind.isDefined)}>
          {errorMessage.bind.getOrElse("")}
        </div>
      </form>
      <div class="padding20"></div>
      <div></div>
      <button class="btn btn-default btn-block"
              disabled={!allValid.bind}
              onclick={_:Event => onLogin()}>Ok</button>
      <button class="btn btn-danger btn-block">Login With Google</button>
      <a href="#register" class="btn btn-primary btn-block">Register</a>
      <a class="btn btn-info btn-block" href="#playing">Watch Others Play</a>
    </div>
  }

  def onLogin(): Unit = {
    UserAPI.userLogin(username.value, password.value).foreach {
      case Ok(value) =>
        // window.alert(value.toString)
        GlobalValue.updateUserSession()
        if(value.role == UserSession.USER){
          window.location.hash = "user/" + value.id
        }
      case Error(message, _) =>
        errorMessage.value = Some(message)
    }
  }
}
