package com.fang.page

import com.fang.ImplicitConvert._
import com.fang.ajax.UserAPI
import com.fang.page.DomUtil.{bindCheckbox, bindInputValue, hideClassIf, showClassIf}
import com.fang.segment.HeadNavBar
import com.fang.segment.HeadNavBar.NavItem
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, HTMLImageElement, Node}
import org.scalajs.dom.window

class RegisterPage extends Page {
  override def title(): String = "register"

  val username: Var[String] = Var("")
  var password: Var[String] = Var("")
  val password2: Var[String] = Var("")
  val email: Var[String] = Var("")
  val nickname: Var[String] = Var("")
  val customImageUrl: Var[String] = Var("")

  @dom val userValid: Binding[Boolean] =  username.bind.length > 0
  @dom val samePassword: Binding[Boolean] = password.bind == password2.bind && password.bind.length > 0
  val emailValid: Binding[Boolean] = DomUtil.validEmail(email)
  @dom val allValid: Binding[Boolean] = samePassword.bind && emailValid.bind

  val defaultUrl: Var[Boolean] = Var(true)

  @dom val imageUrl: Binding[String] = {
    if(defaultUrl.bind) UserAPI.defaultImageUrl(username.bind)
    else customImageUrl.bind
  }

  val searchName: Var[String] = Var("")
  val searchResult: Vars[String] = Vars()

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
          <div class="form-group">
            <label class="control-label" for="id-nickname">Nickname</label>
            <input type="text" class="form-control" id="id-nickname" placeholder={username.bind}
                   oninput={bindInputValue(_:Event, nickname)} />
          </div>
          <div class="form-group">
            <label class="control-label" for="id-image">Image Url</label>
            <input type="text" class="form-control" id="id-image"
                   value={imageUrl.bind}
                   oninput={bindInputValue(_:Event, customImageUrl)} />
          </div>
          <div class="checkbox">
            <label><input type="checkbox" checked={defaultUrl.value} onclick={bindCheckbox(_:Event, defaultUrl)}/>Use default</label>
            <button type="button" class="btn btn-sm btn-default"
                    data:data-toggle="modal" data:data-target="#myModal">Use Flickr for image</button>
          </div>
          <img src={imageUrl.bind} class="img-thumbnail thumbImage" />
        </form>
        <button class="btn btn-success btn-block" disabled={!allValid.bind}>
          <span class="glyphicon glyphicon-pencil"></span> Register
        </button>
        <button class="btn btn-danger btn-block" onclick={_:Event => window.history.back()}>
          <span class="glyphicon glyphicon-log-out"></span> Cancel
        </button>
      </div>

      <div class="modal fade" id="myModal" data:tabindex="-1" data:role="dialog">
        <div class="modal-dialog" data:role="document">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data:data-dismiss="modal"><span>&times;</span></button>
              <h4 class="modal-title" id="myModalLabel">Modal title</h4>
            </div>
            <div class="modal-body">
              <div class="form-inline">
                <input type="text" class="form-control" id="id-searchname"
                       oninput={bindInputValue(_:Event, searchName)} />
                <button class="btn btn-default" onclick={_:Event => flickrSearch()}>
                  <span class="glyphicon glyphicon-search"></span>
                </button>
              </div>
              <div>
                { for(url <- searchResult)
                yield <img src={url} data:data-dismiss="modal"
                           class="img-thumbnail thumbImage"
                           onclick={pickImage(_:Event)} /> }
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-default" data:data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  }

  def flickrSearch(): Unit = {
    val query = searchName.value
    UserAPI.flickerSearchImages(query).foreach{ seq =>
      DomUtil.assignVars(searchResult, seq)
    }
  }

  def pickImage(event: Event): Unit = {
    val url = event.target.asInstanceOf[HTMLImageElement].src
    defaultUrl.value = false
    customImageUrl.value = url
  }
}
