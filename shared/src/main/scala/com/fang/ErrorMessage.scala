package com.fang

import upickle.default._

case class ErrorMessage(message: String)

object ErrorMessage {
  implicit val wrapper:Writer[ErrorMessage] = macroRW[ErrorMessage]
}
