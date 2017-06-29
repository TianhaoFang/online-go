package com.fang

import com.fang.UserModel.{NoPassword, View}
import upickle.default._

case class UserModel
(
  username: String,
  password: String,
  nickname: String,
  email: String,
  google_id: Option[String],
  image_url: Option[String]
) {
  def noPassword: NoPassword = {
    NoPassword(username, nickname, email, google_id, image_url)
  }
  def toView = View(username, nickname, image_url)
}

object UserModel {
  implicit val mapper1: ReadWriter[UserModel] = macroRW[UserModel]
  implicit val mapper2: ReadWriter[NoPassword] = macroRW[NoPassword]

  case class NoPassword
  (
    username: String,
    nickname: String,
    email: String,
    google_id: Option[String],
    image_url: Option[String]
  ){
    def toView = View(username, nickname, image_url)
  }

  case class View
  (
    username: String,
    nickname: String,
    image_url: Option[String]
  )
}