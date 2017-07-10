package com.fang.page

import com.fang.ImplicitConvert._
import com.fang.page.DomUtil.{bindInputValue, hideClassIf, showClassIf}
import com.fang.segment.HeadNavBar
import com.fang.segment.HeadNavBar.NavItem
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.window
import org.scalajs.dom.raw.{Event, Node}

class RegisterPage extends Page {
  override def title(): String = "register"

  val username: Var[String] = Var("")
  var password: Var[String] = Var("")
  val password2: Var[String] = Var("")
  val email: Var[String] = Var("")

  @dom val userValid: Binding[Boolean] =  username.bind.length > 0
  @dom val samePassword: Binding[Boolean] = password.bind == password2.bind && password.bind.length > 0
  val emailValid: Binding[Boolean] = DomUtil.validEmail(email)
  @dom val allValid: Binding[Boolean] = samePassword.bind && emailValid.bind

  @dom override def onLoad(): Binding[Node] = {
    <div>
      {HeadNavBar(Vars(
      NavItem("Watch Others"),
      NavItem("Login", "#login")
    )).bind}

      <div class="container with-padding">
        <h1><b>Register</b></h1>
        <div class="padding10"></div>
        <form name="registerForm">
          <div class="form-group">
            <label class="control-label" for="id-username">Username</label>
            <input type="text" class="form-control" id="id-username" oninput={bindInputValue(_:Event, username)} />
          </div>
          <p class={showClassIf("text-danger", "hide", userValid).bind}>
            username required
          </p>
          <div class="form-group">
            <label class="control-label" for="id-password">Password</label>
            <input type="password" class="form-control" id="id-password" oninput={bindInputValue(_:Event, password)} />
          </div>
          <div class={hideClassIf("form-group", "has-error", samePassword).bind}>
            <label class="control-label" for="id-password2">Confirm Password</label>
            <input type="password" class="form-control" id="id-password2" oninput={bindInputValue(_:Event, password2)} />
          </div>
          <p class={showClassIf("text-danger", "hide", samePassword).bind}>
            password is not same and should not be empty
          </p>
          <div class={hideClassIf("form-group", "has-error", emailValid).bind}>
            <label class="control-label" for="id-email">Email</label>
            <input type="text" class="form-control" id="id-email" oninput={bindInputValue(_:Event, email)} />
          </div>
          <p class={showClassIf("text-danger", "hide", emailValid).bind}>
            email should be valid
          </p>
        </form>
        <button class="btn btn-success btn-block" disabled={!allValid.bind}>
          <span class="glyphicon glyphicon-pencil"></span> Register
        </button>
        <button class="btn btn-danger btn-block" onclick={_:Event => window.history.back()}>
          <span class="glyphicon glyphicon-log-out"></span> Cancel
        </button>
      </div>
    </div>
  }
}
