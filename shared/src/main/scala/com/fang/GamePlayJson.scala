package com.fang

import java.sql.Timestamp
import java.util.UUID

import upickle.default._

case class GamePlayJson
(
  id: String,
  first_user: String,
  second_user: String,
  status: String,
  first_win: Option[Boolean],
  start_time: Timestamp,
  steps: List[Step]
){
  def toUuidId = GamePlayModel(UUID.fromString(id), first_user, second_user, status, first_win, start_time, steps)
}

object GamePlayJson {
  implicit val wrapper: ReadWriter[GamePlayJson] = macroRW[GamePlayJson]
}