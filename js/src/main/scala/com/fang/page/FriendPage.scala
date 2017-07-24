package com.fang.page
import com.fang.FriendModel
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}
import com.fang.ImplicitConvert._
import com.fang.ajax.{FriendAPI, UserAPI}
import com.fang.data.AjaxResult.{Error, Ok}
import com.fang.page.DomUtil.bindInputValue
import com.fang.segment.UserStatusNavBar
import com.thoughtworks.binding.Binding.{Var, Vars}
import org.scalajs.dom.window

class FriendPage(val userId: String) extends Page{
  override def title(): String = "Friends"

  val invited: Vars[FriendModel] = Vars()
  val friends: Vars[String] = Vars()
  var token = 0
  val searchValue: Var[String] = Var("")
  val waitingValue: Vars[String] = Vars()

  def updateInvited(): Unit = {
    FriendAPI.getInvented(userId).foreach {
      case Ok(value) =>
        DomUtil.assignVars(invited, value)
      case Error(message, _) =>
        window.alert(message)
        DomUtil.assignVars(invited, Seq())
    }
  }

  def updateFriend(): Unit = {
    FriendAPI.getAllFriends(userId).foreach {
      case Ok(value) =>
        val result = value.map(m => {
          if(m.user1 == userId) m.user2
          else m.user1
        })
        DomUtil.assignVars(friends, result)
      case Error(message, code) =>
        window.alert(message)
        DomUtil.assignVars(friends, Seq())
    }
  }

  def bothUpdate(): Unit = {
    updateInvited()
    updateFriend()
  }

  @dom override def onLoad(): Binding[Node] = {
    token = window.setInterval(() => {bothUpdate()}, 10000)
    bothUpdate()

    <div>
      {UserStatusNavBar().bind}

      <div class="container with-padding">
        <button data:data-toggle="modal" class="btn btn-default btn-block"
                data:data-target="#myModal3">Find Friends</button>
        <h1>Invitation</h1>
        <table class="table table-striped">
          <thead>
            <tr>
              <th>Name</th>
              <th>Accept</th>
              <th>Reject</th>
            </tr>
          </thead>
          <tbody>
            {for(s <- invited) yield <tr>
              <td>{if(s.user1 == userId) s.user2 else s.user1}</td>
              {if(s.user1 == userId){
                <td>Wait to Accept</td>
              }else{
                <td><button class="btn btn-sm btn-success" onclick={_:Event => onAccept(s.user1)}>
                  Accept</button></td>
              }}
              <td><button class="btn btn-sm btn-danger"
                          onclick={_:Event => onReject({if(s.user1 == userId) s.user2 else s.user1})}>
                Reject</button></td>
            </tr>}
          </tbody>
        </table>

        <h1>Friends</h1>
        <table class="table table-striped">
          <thead>
            <tr>
              <th>Name</th>
              <th>Message</th>
              <th>Delete</th>
            </tr>
          </thead>
          <tbody>{
            for(name <- friends) yield <tr>
              <td>{name}</td>
              <td><button class="btn btn-sm btn-default" onclick={_:Event => onInvite(name)}>Invite</button></td>
              <td><button class="btn btn-sm btn-danger" onclick={_:Event => onDelete(name)}>Delete</button></td>
            </tr>}
          </tbody>
        </table>
      </div>

      <div class="modal fade" id="myModal3" data:tabindex="-1" data:role="dialog">
        <div class="modal-dialog" data:role="document">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data:data-dismiss="modal"><span>&times;</span></button>
              <h4 class="modal-title">Find Friends</h4>
            </div>
            <div class="modal-body">
              <div class="form-inline">
                <input type="text" class="form-control" id="id-searchname"
                       oninput={bindInputValue(_:Event, searchValue)} />
                <button class="btn btn-default" onclick={_:Event => onSearchName()}>
                  <span class="glyphicon glyphicon-search"></span>
                </button>
              </div>
              <ul>{
                for(s <- waitingValue) yield
                  <li>
                    <a data:data-dismiss="modal" onclick={_:Event => makeInvite(s)}>{s}</a>
                  </li>
                }</ul>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" data:data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal fade" id="myModal4" data:tabindex="-1" data:role="dialog">
        <div class="modal-dialog" data:role="document">
          <div class="modal-content">
            <div class="modal-header">
              <button type="button" class="close" data:data-dismiss="modal"><span>&times;</span></button>
              <h4 class="modal-title">Invite To Play</h4>
            </div>
            <div class="modal-body">
              <div class="form-inline">
                <input type="text" class="form-control" id="other"
                       oninput={bindInputValue(_:Event, searchValue)} />
                <button class="btn btn-default" onclick={_:Event => onSearchName()}>
                  <span class="glyphicon glyphicon-search"></span>
                </button>
              </div>
              <table><tbody>{
                for(s <- waitingValue) yield
                  <li>
                    <button class="btn btn-info" data:data-dismiss="modal"
                            onclick={_:Event => makeInvite(s)}>{s}</button>
                  </li>
                }</tbody></table>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-danger" data:data-dismiss="modal">Close</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  }

  override def onUnload(feedback: Page.Feedback): Unit = {
    window.clearInterval(token)
  }

  def makeInvite(str: String): Unit = {
    FriendAPI.makeInvite(userId, str).foreach{ _ =>
      bothUpdate()
    }
  }

  def onSearchName(): Unit = {
    if(searchValue.value.trim() == "") return
    UserAPI.searchName(searchValue.value).foreach {
      case Ok(value) =>
        DomUtil.assignVars(waitingValue, value)
      case Error(message, _) =>
        window.alert(message)
        DomUtil.assignVars(waitingValue, Seq())
    }
  }

  def onAccept(name: String): Unit = {
    FriendAPI.acceptInvention(userId, name).foreach {
      case Ok(value) =>
        window.alert(value)
        bothUpdate()
      case Error(message, _) =>
        window.alert(message)
        bothUpdate()
    }
  }

  def onReject(name: String): Unit = {
    FriendAPI.deleteRelation(userId, name).foreach{
      case Ok(value) =>
        window.alert(value)
        bothUpdate()
      case Error(message, _) =>
        window.alert(message)
        bothUpdate()
    }
  }

  def onInvite(name: String): Unit = {
    window.alert("onMessage: " + name)
  }

  def onDelete(name: String): Unit = {
    onReject(name)
  }
}
