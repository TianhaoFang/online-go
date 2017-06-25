package com.fang

case class UserSession(id: String, role: String = "user", tag: String = UserSession.rand)

object UserSession {
  def rand: String = Math.random().toString + ":ect"
}
