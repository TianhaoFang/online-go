package com.fang.page

import com.fang.ImplicitConvert._
import com.fang.{Password, UserModel}
import com.fang.ajax.UserAPI
import com.fang.data.AjaxResult.{AjaxResult, Error, Ok}
import com.fang.page.DomUtil.{bindCheckbox, bindInputValue, hideClassIf, showClassIf}
import com.fang.segment.HeadNavBar
import com.fang.segment.HeadNavBar.NavItem
import com.thoughtworks.binding.Binding.{BindingInstances, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, HTMLImageElement, Node}
import org.scalajs.dom.window

import scala.concurrent.Future

class RegisterPage extends Page {
  def isRegister: Boolean = true
  val isViewOnly: Var[Boolean] = Var(true)

  override def title(): String = "register"

  val username: Var[String] = Var("")
  var password: Var[String] = Var("")
  val password2: Var[String] = Var("")
  val email: Var[String] = Var("")
  val nickname: Var[String] = Var("")
  val customImageUrl: Var[String] = Var("")
  val oldPassword: Var[String] = Var("")
  val defaultUrl: Var[Boolean] = Var(true)

  @dom val userValid: Binding[Boolean] =  username.bind.length > 0
  @dom val samePassword: Binding[Boolean] = password.bind == password2.bind && password.bind.length > 0
  val emailValid: Binding[Boolean] = DomUtil.validEmail(email)
  @dom val allValid: Binding[Boolean] = (samePassword.bind || !isRegister) && emailValid.bind

  @dom val imageUrl: Binding[String] = {
    if(defaultUrl.bind) UserAPI.defaultImageUrl(username.bind)
    else customImageUrl.bind
  }

  val searchName: Var[String] = Var("")
  val searchResult: Vars[String] = Vars()
  val invalidUser: Var[Option[String]] = Var(None)

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
            {
              if(isRegister){
                  <input type="text" class="form-control" id="id-username"
                         oninput={bindInputValue(_:Event, username)}
                         value={username.bind} />
              }else{
                  <p class="form-control-static form-control" id="id-username">{username.bind}</p>
              }
            }
          </div>
          <p class={showClassIf("text-danger", "hide", userValid.bind)}>
            username required
          </p>
          <p class={hideClassIf("text-danger", "hide", invalidUser.bind.isDefined)}>
            {invalidUser.bind.getOrElse("")}
          </p>
          <div class={showClassIf("form-group", "hide", !isRegister)}>
            <label class="control-label" for="id-password">Password</label>
            <input type="password" class="form-control" id="id-password"
                   oninput={bindInputValue(_:Event, password)}
                   value={password.bind} />
          </div>
          <div class={showClassIf(hideClassIf("form-group", "has-error", samePassword.bind), "hide", !isRegister)}>
            <label class="control-label" for="id-password2">Confirm Password</label>
            <input type="password" class="form-control" id="id-password2"
                   oninput={bindInputValue(_:Event, password2)}
                   value={password2.bind} />
          </div>
          <p class={showClassIf("text-danger", "hide", samePassword.bind || !isRegister)}>
            password is not same and should not be empty
          </p>
          <div class={hideClassIf("form-group", "hide", !isRegister)}>
            <label class="control-label">Password</label>
            <button type="button" data:data-toggle="modal" data:data-target="#myModal2"
                    class="btn btn-default btn-block">
              Update Password</button>
          </div>
          <div class={hideClassIf("form-group", "has-error", emailValid.bind)}>
            <label class="control-label" for="id-email">Email</label>
            {
            if(isRegister || !isViewOnly.bind){
                <input type="text" class="form-control" id="id-email"
                       oninput={bindInputValue(_:Event, email)} value={email.bind} />
            }else{
              <p class="form-control-static form-control">{email.bind}</p>
            }}
          </div>
          <p class={showClassIf("text-danger", "hide", emailValid.bind)}>
            email should be valid
          </p>
          <div class="form-group">
            <label class="control-label" for="id-nickname">Nickname</label>
            {
            if(isRegister || !isViewOnly.bind){
                <input type="text" class="form-control" id="id-nickname" placeholder={username.bind}
                       oninput={bindInputValue(_:Event, nickname)} value={nickname.bind} />
            }else{
              <p class="form-control-static form-control">{ if(nickname.bind == "") username.bind else nickname.bind}</p>
            }}

          </div>
          <div class="form-group">
            <label class="control-label" for="id-image">Image Url</label>
            {
            if(isRegister || !isViewOnly.bind){
              <input type="text" class="form-control" id="id-image"
                     value={imageUrl.bind}
                     oninput={bindInputValue(_:Event, customImageUrl)} />
            }else{
              <p class="form-control-static form-control">{imageUrl.bind}</p>
            }}

          </div>
          <div class="checkbox">
            <label><input type="checkbox" checked={defaultUrl.bind} onclick={bindCheckbox(_:Event, defaultUrl)}/>Use default</label>
            <button type="button" class={showClassIf("btn btn-sm btn-default", "hide", !isRegister && isViewOnly.bind)}
                    data:data-toggle="modal" data:data-target="#myModal">Use Flickr for image</button>
          </div>
          <img src={imageUrl.bind} class="img-thumbnail thumbImage" />
        </form>
        {
          if(isRegister){
            <button class="btn btn-success btn-block" disabled={!allValid.bind} onclick={_:Event => onRegister()}>
              <span class="glyphicon glyphicon-pencil"></span> Register
            </button>
          }else if(!isViewOnly.bind){
            <button class="btn btn-success btn-block" disabled={!allValid.bind} onclick={_:Event => onUpdate()}>
              <span class="glyphicon glyphicon-pencil"></span> Update
            </button>
          }else{
            <button class="btn btn-info btn-block" onclick={_:Event => onModify()}>
              <span class="glyphicon glyphicon-pencil"></span> Modify
            </button>
          }
        }
        <button class="btn btn-danger btn-block" onclick={_:Event => window.history.back()}>
          <span class="glyphicon glyphicon-log-out"></span> Back
        </button>
      </div>

      <div class="modal fade" id="myModal" data:tabindex="-1" data:role="dialog">
        <div class="modal-dialog" data:role="document">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data:data-dismiss="modal"><span>&times;</span></button>
              <h4 class="modal-title">Flickr Search</h4>
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
              <button type="button" class="btn btn-danger" data:data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal fade" id="myModal2" data:tabindex="-1" data:role="dialog">
        <div class="modal-dialog" data:role="document">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data:data-dismiss="modal"><span>&times;</span></button>
              <h4 class="modal-title">Update Password</h4>
            </div>
            <div class="modal-body">
              <form>
                <div class="form-group">
                  <label class="control-label" for="md-oldpassword">Old Password</label>
                  <input type="password" class="form-control" id="md-oldpassword"
                         oninput={bindInputValue(_:Event, oldPassword)}
                         value={oldPassword.bind} />
                </div>
                <div class="form-group">
                  <label class="control-label" for="md-password">New Password</label>
                  <input type="password" class="form-control" id="md-password"
                         oninput={bindInputValue(_:Event, password)}
                         value={password.bind} />
                </div>
                <div class="form-group">
                  <label class="control-label" for="md-password2">Confirm Password</label>
                  <input type="password" class="form-control" id="md-password2"
                         oninput={bindInputValue(_:Event, password2)}
                         value={password2.bind} />
                </div>
              </form>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-default"
                      disabled={!samePassword.bind || isRegister} onclick={_:Event => changePassword()}>
                Update Password</button>
              <button type="button" class="btn btn-danger" data:data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  }

  def flickrSearch(): Unit = {
    val query = searchName.value
    if(searchName.value.length() <= 0) return
    UserAPI.flickerSearchImages(query).foreach{ seq =>
      DomUtil.assignVars(searchResult, seq)
    }
  }

  def pickImage(event: Event): Unit = {
    val url = event.target.asInstanceOf[HTMLImageElement].src
    defaultUrl.value = false
    customImageUrl.value = url
  }

  @dom def onRegister(): Unit = {
    window.alert(s"onRegister is called with\n" +
      s"username:${username.value} password:${password.value} password2:${password2.value}\n" +
      s"nickname: ${nickname.value} email: ${email.value} image_url:" + imageUrl.bind)
    val model = UserModel(
      username = username.value,
      password = password.value,
      nickname = nickname.value,
      email = email.value,
      google_id = None,
      image_url = Some(imageUrl.bind)
    )
    UserAPI.createUser(model).map{
      case Ok(value) =>
        invalidUser.value = None
        window.alert(value)
        window.location.hash = "user/" + model.username
      case Error(message, _) =>
        window.alert(message)
        invalidUser.value = Some(message)
    }
  }

  def forceUpdate(bind: Var[String]): Unit = {
    val t = bind.value
    bind.value = ""
    bind.value = t
  }

  def onModify(): Unit = {
    isViewOnly.value = false
    forceUpdate(email)
    forceUpdate(nickname)
    forceUpdate(customImageUrl)
    defaultUrl.value = !defaultUrl.value
    defaultUrl.value = !defaultUrl.value
  }

  @dom def onUpdate(): Unit = {
//    window.alert(s"onUpdate is called with\n" +
//      s"username:${username.value} password:${password.value} password2:${password2.value}\n" +
//      s"nickname: ${nickname.value} email: ${email.value} image_url:" + imageUrl.bind)
    UserAPI.updateUser(UserModel.NoPassword(
      username = username.value,
      nickname = nickname.value,
      email = email.value,
      google_id = None,
      image_url = Some(imageUrl.bind)
    )).map {
      case Ok(value) =>
        window.alert("update success")
        initFieldWithModel(value)
      case Error(message, _) =>
        initWithId(username.value)
        window.alert(message)
    }
  }

  def changePassword(): Unit = {
    // window.alert(s"changePassword old:${oldPassword.value} new:${password.value} confirm:${password2.value}")
    UserAPI.updatePassword(username.value, Password(password.value, oldPassword.value)).foreach {
      case Ok(value) =>
        window.alert(value)
      case Error(message, _) =>
        window.alert(message)
    }
  }

  def initFieldWithModel(model: UserModel.NoPassword): Unit = {
    username.value = model.username
    password.value = ""
    password2.value = ""
    email.value = model.email
    nickname.value = model.nickname
    oldPassword.value = ""
    val url = model.image_url.getOrElse("")
    if(url == UserAPI.defaultImageUrl(model.username)){
      defaultUrl.value = true
      customImageUrl.value = ""
    }else{
      defaultUrl.value = false
      customImageUrl.value = url
    }
    isViewOnly.value = true
  }

  def fetchUserData(username: String): Future[AjaxResult[UserModel.NoPassword]] = {
    UserAPI.getUser(username).map(_ map {
      case Left(a) =>
        window.alert("you are not current login, please login")
        window.location.hash = "login"
        UserModel.NoPassword(a.username, a.nickname, "", None, Some(""))
      case Right(b) =>
        b
    })
  }

  def initWithId(username: String): Unit = {
    fetchUserData(username).foreach {
      case Ok(value) =>
        initFieldWithModel(value)
      case Error(msg, _) =>
        window.alert(msg)
        window.location.hash = "login"
    }
  }
}
