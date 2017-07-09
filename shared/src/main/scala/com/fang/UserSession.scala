package com.fang

import upickle.default
import upickle.default._

case class UserSession(id: String, role: String = "user", tag: String = UserSession.rand)

object UserSession {
  def rand: String = Math.random().toString + ":ect"
  implicit val wrapper: ReadWriter[UserSession] = macroRW[UserSession]

  val USER = "user"
  val ADMIN = "admin"
}
