package com.fang.page

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._

class LoginPage extends Page {
  override def title(): String = "login"

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
          <input type="text" name="username" class="form-control" placeholder="Your name"/>
        </div>
        <div class="form-group">
          <label class="control-label">Password</label>
          <input type="password" name="password" class="form-control" placeholder="Password"/>
        </div>
      </form>
      <div class="padding20"></div>
      <button class="btn btn-default btn-block">Ok</button>
      <button class="btn btn-danger btn-block">Login With Google</button>
      <button class="btn btn-info btn-block">Watch Others Play</button>
    </div>
  }
}
