package com.fang.ajax

import com.fang.GamePlayJson
import com.fang.data.AjaxResult
import com.fang.data.AjaxResult.AjaxResult
import com.fang.game.Step
import org.scalajs.dom.ext.Ajax
import upickle.default.{read, write}
import org.scalajs.dom.window

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object GamePlayAPI {
  def getGamePlay(gameId: String): Future[AjaxResult[GamePlayJson]] =
    Ajax.get(s"/gameplay/$gameId")
      .map(AjaxResult.mapToResult(read[GamePlayJson]))
      .recover(AjaxResult.recovery)

  def getGameStep(gameId: String, index: Int): Future[AjaxResult[Step]] =
    Ajax.get(s"/gameplay/$gameId/step/$index")
      .map(AjaxResult.mapToResult(read[Step]))
      .recover(AjaxResult.recovery)

  def putGameStep(gameId: String, index: Int, body: Step): Future[AjaxResult[Step]] =
    Ajax.put(s"/gameplay/$gameId/step/$index", write(body))
      .map(AjaxResult.mapToResult(read[Step]))
      .recover(AjaxResult.recovery)

  def queryPlayingGame(): Future[AjaxResult[Seq[GamePlayJson]]] =
    Ajax.get("/playing")
      .map(AjaxResult.mapToResult(read[Seq[GamePlayJson]]))
      .recover(AjaxResult.recovery)

  def wsPath(gameId: String): String = UserStatusAPI.wsUrl(s"/gameplay/$gameId/ws")

  abstract class GamePlayListener(val gameId: String) extends WSConnection[(Step, Int), String](wsPath(gameId)) {
    override def decode(input: String): (Step, Int) = read[(Step, Int)](input)

    override def encode(output: String): String = output
  }

}
