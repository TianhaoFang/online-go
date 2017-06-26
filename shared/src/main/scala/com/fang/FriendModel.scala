package com.fang

import upickle.default
import upickle.default._

case class FriendModel (user1: String, user2: String, url: String, accepted: Boolean){
  def swapUser = FriendModel(user2, user1, url, accepted)
}

object FriendModel{
  implicit val wrapper: ReadWriter[FriendModel] = macroRW[FriendModel]
}
