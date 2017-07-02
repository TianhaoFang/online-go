package com.fang

import upickle.default
import upickle.default._

case class LoginRequest (username: String, password: String)

object LoginRequest {
  implicit val pkl: ReadWriter[LoginRequest] = macroRW[LoginRequest]
}