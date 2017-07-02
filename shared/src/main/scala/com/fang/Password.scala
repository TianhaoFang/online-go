package com.fang

import upickle.default._

case class Password (password: String, oldPassword: String)

object Password {
  implicit val wrapper: ReadWriter[Password] = macroRW[Password]
}
