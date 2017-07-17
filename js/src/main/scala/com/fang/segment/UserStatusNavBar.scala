package com.fang.segment

import com.fang.UserStatus.{Idle, Playing, Waiting}
import com.fang.data.GlobalValue
import com.fang.segment.HeadNavBar.NavItem
import com.thoughtworks.binding.Binding.{BindingInstances, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._
import com.fang.UserStatus
import com.fang.page.DomUtil

object UserStatusNavBar {
  @dom def apply(): Binding[Node] = {
    val items: Vars[NavItem] = Vars()
    @dom val userStatus: Binding[Option[UserStatus]] = GlobalValue.userStatus.bind
    @dom val seq: Binding[Seq[NavItem]] = (userStatus.bind match {
      case Some(status: UserStatus) =>
        val userId = status.userId
        (if(status.isIdle) Seq(NavItem("Find Game", s"#user/$userId/status"))
        else if(status.isWaiting) Seq(NavItem("Waiting " + status.waitingOn.get, s"#user/$userId/status"))
        else if(status.isPlaying) Seq(NavItem("Return to Game", s"#user/$userId/status"))
        else Seq()) ++ Seq(
          NavItem("Profile", "#user/" + userId),
          NavItem("Friends", s"#user/$userId/friends"),
          NavItem("logout", "#logout")
        )
      case None =>
        Seq(
          NavItem("Login", "#login")
        )
    }) ++ Seq(NavItem("Watch Others", "#playing"))
    val result: Binding[(Vars[NavItem], Double)] = BindingInstances.map(seq)(s => {
      DomUtil.assignVars(items, s)
      (items, Math.random())
    })
    HeadNavBar(result.bind._1).bind
  }
}
