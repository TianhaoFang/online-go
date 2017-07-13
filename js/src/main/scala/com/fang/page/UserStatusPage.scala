package com.fang.page

import com.fang
import com.fang.data.GlobalValue
import com.fang.{Main, page}
import com.thoughtworks.binding.Binding.{BindingInstances, Var}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import org.scalajs.dom.window
import com.fang.ImplicitConvert._
import com.fang.page.UserStatusPage.AlreadyPlayPage

class UserStatusPage(val userId: String) extends Page {
  var oldPage: Page = new Main.NotFound("Loading")

  override def title(): String = "User Status"

  @dom val findPage: Binding[(Page, Double)] = {
    val result: Page = GlobalValue.userStatus.bind match {
      case Some(x) =>
        if (x.isIdle) new ChooseGamePage(userId)
        else if (x.isWaiting) new WaitingPage(x.waitingOn.get, x.userId)
        else if(x.isPlaying) new AlreadyPlayPage(x.playOn.get, x.userId)
        else new Main.NotFound("invalid user status " + x.toString)
      case None => new Main.NotFound("not found for the user status")
    }
    oldPage.onUnload(new Page.Feedback())
    oldPage = result
    (result, Math.random())
  }

  @dom override def onLoad(): Binding[Node] = {
    findPage.bind._1.onLoad().bind
  }

  @dom override def onUnload(feedback: Page.Feedback): Unit = {
    findPage.bind._1.onUnload(feedback)
  }
}

object UserStatusPage {
  class AlreadyPlayPage(gameId: String, userId: String) extends Page{
    override def title(): String = "playing"

    @dom override def onLoad(): Binding[Node] = {
      window.location.hash = s"game/$gameId"
      <div></div>
    }
  }
}