package com.fang

import java.sql.Timestamp
import java.util.UUID

import upickle.default._
import upickle.{Js, default}

case class GamePlayModel
(
  id: UUID,
  first_user: String,
  second_user: String,
  status: String,
  first_win: Option[Boolean],
  start_time: Timestamp,
  steps: List[Step]
){
  def toStrId = GamePlayJson(id.toString, first_user, second_user, status, first_win, start_time, steps)
}
