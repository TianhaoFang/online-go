package com.fang

import upickle.default.macroRW
import upickle.default.ReadWriter

object Test{
  val text = "It works sadasdad eeec"

  case class User(name: String, age: Int)

  object User{
    implicit val pkl: ReadWriter[User] = macroRW[User]
  }
}
