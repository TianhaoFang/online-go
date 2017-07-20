package com.fang.page
import com.fang.GamePlayJson
import com.fang.segment.{PlayReviewTable, UserStatusNavBar}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._
import com.fang.ajax.GamePlayAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.thoughtworks.binding.Binding.Vars
import org.scalajs.dom.window

class PlayingGamePage extends Page{
  var intervalToken = 0
  val games: Vars[GamePlayJson] = Vars()

  def searchGame(): Unit = {
    GamePlayAPI.queryPlayingGame().map {
      case Ok(value) =>
        DomUtil.assignVars(games, value)
      case Error(message, code) =>
        window.alert(message)
        DomUtil.assignVars(games, Seq())
    }
  }

  override def title(): String = "Recent Games"

  @dom override def onLoad(): Binding[Node] = {
    intervalToken = window.setTimeout(() => {searchGame()}, 10000)
    searchGame()

    <div>
      {UserStatusNavBar().bind}
      <div class ="container with-padding">
        <h1>Recent Games</h1>
        {PlayReviewTable(games).bind}
      </div>
    </div>
  }

  override def onUnload(feedback: Page.Feedback): Unit = {
    window.clearInterval(intervalToken)
  }
}
