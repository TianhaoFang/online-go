package com.fang

import java.sql.Timestamp
import java.util.UUID

import upickle.default._

case class GamePlayModel
(
  id: UUID,
  first_user: String,
  second_user: String,
  status: String,
  first_win: Option[Boolean],
  start_time: Timestamp,
  steps: List[Step]
)

object GamePlayModel{
  implicit val wrapper: ReadWriter[GamePlayModel] = macroRW[GamePlayModel]
}
