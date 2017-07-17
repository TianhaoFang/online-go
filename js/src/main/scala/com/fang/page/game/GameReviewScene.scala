package com.fang.page.game

import com.fang.GamePlayJson
import com.fang.ImplicitConvert._
import com.fang.game.{GameBoard, GameStatus}
import com.fang.page.Page
import com.fang.segment.{BasicBoard, ProgressBar, UserStatusNavBar}
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}

class GameReviewScene(val gameId: String, val gamePlay: GamePlayJson) extends Page {
  override def title(): String = "Game View " + gameId

  /*val gamePlay: GamePlayJson = GamePlayJson(
    id = gameId,
    first_user = "aaa",
    second_user = "bbb",
    status = GamePlayJson.END,
    rule = Rules.gomoku.name,
    first_win = Some(true),
    start_time = "",
    steps = List(Step(1, 1), Step(1, 2), Step(2, 2), Step(1, 3), Step(3, 3), Step(1, 4),
      Step(4, 4), Step(1, 5), Step(5, 5))
  )*/

  @dom val steps: Binding[Seq[GameBoard]] = {
    if (gamePlay == null) Seq()
    else {
      val gameStatus = GameStatus(gamePlay.rule)
      var stepCount = 0
      var result: Seq[GameBoard] = Seq()
      for (step <- gamePlay.steps) {
        gameStatus.put(step, stepCount)
        stepCount += 1
        result = result :+ gameStatus.getGameBoard.copy()
      }
      result
    }
  }

  val currentStep: Var[Int] = Var(0)
  @dom val currentBoard: Binding[Option[GameBoard]] = {
    val currentIndex = currentStep.bind
    val boards = steps.bind
    if (boards.isEmpty) None
    else {
      val length = boards.length
      val index = Math.min(length - 1, currentIndex)
      Some(boards(index))
    }
  }

  @dom val rowBoard: Binding[(GameBoard, Int)] = (currentBoard.bind.orNull, currentStep.bind)

  @dom override def onLoad(): Binding[Node] = {
    <div>
      {UserStatusNavBar().bind}
      <div class="container with-padding">
        {
        if (currentBoard.bind.isDefined) BasicBoard(rowBoard).bind
        else <p>Loading</p>
        }
      </div>

      <nav class="navbar navbar-default navbar-fixed-bottom">
        <div class="container">
          <div class="row">
            <button class="col-xs-2 btn btn-info navbar-btn"
                    disabled={currentStep.bind <= 0}
                    onclick={_:Event => onPrevStep()}>Prev</button>
            <div class="col-xs-6 navbar-progress">
              {ProgressBar(currentStep.bind + 1, steps.bind.length).bind}
            </div>
            <button class="col-xs-2 btn btn-info navbar-btn"
                    disabled={currentStep.bind >= steps.bind.length - 1}
                    onclick={_:Event => onNextStep()}>Next</button>
          </div>
        </div>
      </nav>
    </div>
  }

  @dom def onNextStep(): Unit = {
    currentStep.value = Math.min(currentStep.value + 1, steps.bind.length - 1)
  }

  @dom def onPrevStep(): Unit = {
    currentStep.value = Math.max(currentStep.value - 1, 0)
  }
}
