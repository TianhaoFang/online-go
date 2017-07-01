package com.fang.game

sealed trait Status {
}

object Status {
  case class Continue() extends Status
  case class BlackWin() extends Status
  case class WriteWin() extends Status
  case class SkipNext() extends Status
}