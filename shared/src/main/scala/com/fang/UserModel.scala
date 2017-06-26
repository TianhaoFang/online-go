package com.fang

import com.fang.UserModel.NoPassword
import upickle.default._

case class UserModel
(
  username: String,
  password: String,
  nickname: String,
  email: String,
  google_id: Option[String],
  image_url: Option[String]
) /*{
  def noPassword(): NoPassword = {
    NoPassword(username, nickname, email, google_id, image_url)
  }
}*/

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
  )
}