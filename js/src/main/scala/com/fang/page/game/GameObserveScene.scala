package com.fang.page.game

import com.fang.GamePlayJson
import com.fang.game.{GameBoard, GameStatus, Status, Step}
import com.fang.page.Page
import com.fang.segment.{BasicBoard, ProgressBar, UserStatusNavBar}
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}
import com.fang.ImplicitConvert._
import com.fang.ajax.GamePlayAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.fang.data.GlobalValue
import com.fang.game.Status.{BlackWin, Continue, SkipNext, WriteWin}
import com.fang.page.game.GameObserveScene.Listener
import org.scalajs.dom.{CloseEvent, window}

class GameObserveScene(val gameId: String, originGamePlay: GamePlayJson, val parent: GamePlayScene) extends Page{
  override def title(): String = "Game Observe" + gameId

  val gamePlay: Var[GamePlayJson] = Var(originGamePlay)
  val gameStatus: Var[(GameStatus, Int)] = Var({
    val result = GameStatus(originGamePlay.rule)
    for(s <- originGamePlay.steps){
      result.put(s, result.step + 1)
    }
    (result, result.step)
  })

  @dom val gameBoard: Binding[(GameBoard, Int)] = {
    val (status, step) = gameStatus.bind
    (status.getGameBoard, step)
  }

  val errorMessage: Var[Option[String]] = Var(None)

  @dom val text: Binding[String] = {
    val (status, _) = gameStatus.bind
    val play = gamePlay.bind
    status.getStatus match {
      case Continue() =>
        val user = if(status.step % 2 == 0)
          "black player:" + play.second_user
        else
          "white player:" + play.first_user
        s"$user is playing"
      case BlackWin() =>
        "black player:" + play.first_user + " is win"
      case WriteWin() =>
        "white player:" + play.second_user + " is win"
      case SkipNext() =>
        "the next step is skipped"
    }
  }

  var connection: Option[GameObserveScene.Listener] = None

  @dom override def onLoad(): Binding[Node] = {
    if(connection.isEmpty) connection = Some(new Listener(gameId, this))

    <div>
      {UserStatusNavBar().bind}
      <div class="container with-padding">
        {BasicBoard(gameBoard).bind}
      </div>

      <nav class="navbar navbar-default navbar-fixed-bottom">
        <div class="container">
          <p class="navbar-text">{text.bind}</p>
          <button class="navbar-btn navbar-right btn btn-danger"
                  onclick={_:Event => window.history.back()}>Back</button>
        </div>
      </nav>
    </div>
  }

  override def onUnload(feedback: Page.Feedback): Unit = {
    closeConnection()
  }

  def showWinMessage(isWriteWin: Boolean): Unit = {
    val message =
      if(isWriteWin) s"second player: ${originGamePlay.second_user}"
      else s"first player: ${originGamePlay.first_user}"
    window.alert(message + " is win!")
  }

  private def closeConnection(): Unit = {
    if(connection.isDefined){
      val oldValue = connection.get
      connection = None
      oldValue.close()
    }
  }
}

object GameObserveScene {
  class Listener(gameId: String, val parent: GameObserveScene) extends GamePlayAPI.GamePlayListener(gameId){
    override def onClose(event: CloseEvent): Unit = {
      println("connection closed")
      parent.closeConnection()
    }

    override def onError(message: String): Unit = {
      window.alert(message)
      window.history.back()
    }

    override def onOpen(event: Event): Unit = {
      println("connection open")
    }

    override def onReceive(data: (Step, Int)): Unit = {
      val (status, _) = parent.gameStatus.value
      if(status.step + 1 < data._2){
        GamePlayAPI.getGamePlay(gameId).foreach {
          case Ok(value) =>
            for((step, index) <- value.steps.zipWithIndex){
              if(index >= status.step + 1){
                status.put(step, index)
              }
            }
            parent.gameStatus.value = (status, status.step)
            checkTerminate(status)
          case Error(message, _) =>
            window.alert(message)
            window.history.back()
        }
      }else if(status.step + 1 == data._2){
        status.put(data._1, data._2)
        parent.gameStatus.value = (status, status.step)
        checkTerminate(status)
      }else{
        // do nothing
        checkTerminate(status)
      }
    }

    def checkTerminate(gameStatus: GameStatus): Unit = {
      if(gameStatus.getStatus == Status.WriteWin() ||
        gameStatus.getStatus == Status.BlackWin()){
        if(gameStatus.getStatus == Status.WriteWin()){
          parent.showWinMessage(true)
        }else{
          parent.showWinMessage(false)
        }
        parent.parent.resetPage()
      }
    }
  }
}