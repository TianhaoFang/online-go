package com.fang.page
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}
import com.fang.ImplicitConvert._
import com.fang.UserStatus
import com.fang.UserStatus.UpdateUS
import com.fang.data.GlobalValue
import com.fang.game.rules.Rules
import com.fang.segment.UserStatusNavBar
import com.thoughtworks.binding.Binding.Vars
import org.scalajs.dom.window

class ChooseGamePage(val userId: String) extends Page{
  val allGames: Vars[String] = Vars()

  override def title(): String = "choose the game"

  @dom override def onLoad(): Binding[Node] = {
    DomUtil.assignVars(allGames, Rules.allRules)

    <div>
      {UserStatusNavBar().bind}

      <div class="container with-padding">
        <h1><b>Find Game</b></h1>
        <div class="padding10"></div>
        <h2>Which game you want to play</h2>
        {
        for(name <- allGames) yield
          <button class="btn btn-info btn-block"
                  onclick={_:Event => onSelectGame(name)}>{name}</button>
        }
        <div class="padding10"></div>
        <button class="btn btn-danger btn-block" onclick={_:Event => window.history.back()}>Back</button>
      </div>
    </div>
  }

  def onSelectGame(name: String):Unit = {
    GlobalValue.userStatusSession match {
      case None =>
        window.alert("please login first")
        window.location.hash = "login"
      case Some(x) =>
        x.sendMessage(UpdateUS(UserStatus.waiting(userId, name)))
    }
  }
}
