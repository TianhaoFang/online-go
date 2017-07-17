package com.fang.page.game

import com.fang.{GamePlayJson, UserStatus}
import com.fang.ImplicitConvert._
import com.fang.ajax.GamePlayAPI
import com.fang.data.AjaxResult.{Error, Ok}
import com.fang.data.GlobalValue
import com.fang.game.{GameBoard, Step}
import com.fang.page.game.GameObserveScene.Listener
import com.fang.segment.BasicBoard.Ctx2D
import com.fang.segment.{BasicBoard, UserStatusNavBar}
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}
import org.scalajs.dom.window

class GamePlayingScene(gameId: String, originGamePlay: GamePlayJson, parent: GamePlayScene) extends
  GameObserveScene(gameId, originGamePlay, parent){
  override def title(): String = "Game Observe" + gameId

  val isPlayerWhite: Boolean = {
    GlobalValue.userStatus.value match {
      case Some(status) =>
        if(status.userId == originGamePlay.first_user){
          false
        }else if(status.userId == originGamePlay.second_user){
          true
        }else{
          window.alert("you are not the player for the game")
          window.history.back()
          false
        }
      case None =>
        window.alert("you are not the player for the game")
        window.history.back()
        false
    }
  }

  @dom val isWhitePlaying: Binding[Boolean] = {
    val (_, step) = gameStatus.bind
    step % 2 == 0
  }

  val nextPiece: Var[Option[Step]] = Var(None)
  val isSubmit: Var[Boolean] = Var(false)

  @dom override def onLoad(): Binding[Node] = {
    if(connection.isEmpty) connection = Some(new Listener(gameId, this))

    <div>
      {UserStatusNavBar().bind}
      <div class="container with-padding">
        {BasicBoard(gameBoard, afterPaint, onBoardClick).bind}
      </div>

      <nav class="navbar navbar-default navbar-fixed-bottom">
        {
        if(isPlayerWhite == isWhitePlaying.bind){
          <div class="container">
            <button class="navbar-btn navbar-right btn btn-success"
                    disabled={nextPiece.bind.isEmpty}
                    onclick={_:Event => onSubmit()}>Submit</button>
            <p class="navbar-text">Is now you playing</p>
          </div>
        }else{
          <div class="container">
            <p class="navbar-text">{text.bind}</p>
          </div>
        }
        }
      </nav>
    </div>
  }

  override def showWinMessage(isWriteWin: Boolean): Unit = {
    if(GlobalValue.userStatusSession.isDefined){
      GlobalValue.userStatusSession.get.sendMessage(UserStatus.QueryUS())
    }
    if(isPlayerWhite == isWriteWin){
      window.alert("you win")
    }else{
      window.alert("you lose")
    }
  }

  @dom override val text: Binding[String] = {
    if(isSubmit.bind){
      "Submitting result"
    }else if(isPlayerWhite != isWhitePlaying.bind){
      "Now opponent: " + (if(isPlayerWhite) originGamePlay.first_user else originGamePlay.second_user) + " is playing"
    }else{
      "waiting"
    }
  }

  def afterPaint(ctx: Ctx2D): Unit = {
    nextPiece.value match {
      case None =>
      case Some(Step(x, y)) =>
        val color = {
          if(isPlayerWhite) "rgba(255, 255, 255, 0.5)"
          else "rgba(0, 0, 0, 0.5)"
        }
        ctx.beginPath()
        ctx.strokeStyle = "red"
        ctx.fillStyle = color
        BasicBoard.fillPieceCircle(ctx, x, y)
        ctx.stroke()
        ctx.fill()
    }
  }

  def onBoardClick(x: Int, y: Int): Unit = {
    val (status, step) = gameStatus.value
    if(isPlayerWhite != (step % 2 == 0)) return
    if(isSubmit.value) return
    if(status.getGameBoard.get(x, y) != GameBoard.EMPTY) return
    if(!status.gameRule.canPlace(status.getGameBoard)(x, y, step + 1)) return
    nextPiece.value = Some(Step(x, y))
    val oldValue = gameStatus.value
    gameStatus.value = (gameStatus.value._1, gameStatus.value._2 - 1)
    gameStatus.value = oldValue
  }

  def onSubmit(): Unit = {
    if(nextPiece.value.isEmpty) return
    val index = gameStatus.value._2 + 1
    val step = nextPiece.value.get
    nextPiece.value = None
    isSubmit.value = true
    GamePlayAPI.putGameStep(gameId, index, step).map {
      case Ok(_) =>
      case Error(message, _) =>
        window.alert(message)
    }.foreach(_ => {isSubmit.value = false})
  }
}
