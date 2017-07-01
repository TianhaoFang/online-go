package models

import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.fang.GamePlayJson
import com.fang.game.Step

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
  def toStrId = GamePlayJson(id.toString, first_user, second_user, status, first_win, GamePlayModel.formatTimeStr(start_time), steps)
}

object GamePlayModel {
  def formatTimeStr(timestamp: Timestamp): String = {
    val ins:Instant = Instant.ofEpochMilli(timestamp.getTime)
    DateTimeFormatter.ISO_INSTANT.format(ins)
  }
  def parseTimeStr(str: String): Timestamp = {
    new Timestamp(Instant.parse(str).toEpochMilli)
  }
  def fromJson(g: GamePlayJson): GamePlayModel = g match {
    case GamePlayJson(id, first_user, second_user, status, first_win, start_time, steps) =>
      GamePlayModel(UUID.fromString(id), first_user, second_user, status, first_win, parseTimeStr(start_time), steps)
  }
}