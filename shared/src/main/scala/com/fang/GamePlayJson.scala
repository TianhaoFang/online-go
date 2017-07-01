package com.fang

import com.fang.game.Step
import upickle.default._

case class GamePlayJson
(
  id: String,
  first_user: String,
  second_user: String,
  status: String,
  first_win: Option[Boolean],
  start_time: String,
  steps: List[Step]
)

object GamePlayJson {
  implicit val wrapper: ReadWriter[GamePlayJson] = macroRW[GamePlayJson]
}