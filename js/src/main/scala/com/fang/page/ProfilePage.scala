package com.fang.page

class ProfilePage(val userId: String) extends RegisterPage{
  override def isRegister: Boolean = false

  override def title(): String = s"Profile for $userId"

  initWithId(userId)
}
