package com.fang.page.game

import com.fang.GamePlayJson
import com.fang.page.Page
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._
import com.fang.ajax.GamePlayAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.fang.data.GlobalValue
import com.fang.page.Page.Feedback
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.window

class GamePlayScene(val gameId: String) extends Page{
  val page: Var[Page] = Var(new LoadingPage)

  override def title(): String = "GamePlay " + gameId

  @dom override def onLoad(): Binding[Node] = {
    resetPage()
    page.bind.onLoad().bind
  }

  def resetPage(): Unit = {
    GamePlayAPI.getGamePlay(gameId).map {
      case Ok(value) =>
        if(value.status == GamePlayJson.PLAYING){
          GlobalValue.userStatus.value match {
            case Some(status) =>
              if(status.userId == value.first_user || status.userId == value.second_user){
                new GamePlayingScene(gameId, value, this)
              }else{
                new GameObserveScene(gameId, value, this)
              }
            case None =>
              new GameObserveScene(gameId, value, this)
          }
        }else{
          new GameReviewScene(gameId, value)
        }
      case Error(message, _) =>
        window.alert(message)
        new LoadingPage("Error:" + message)
    }.foreach{ newPage =>
      page.value.onUnload(new Feedback())
      page.value = newPage
    }
  }

  class LoadingPage(message: String = "Loading") extends Page{
    override def title(): String = GamePlayScene.this.title()

    @dom override def onLoad(): Binding[Node] = {
      <h1 class="">{message}</h1>
    }
  }
}