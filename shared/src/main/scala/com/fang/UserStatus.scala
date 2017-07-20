package com.fang

import com.fang.UserStatus.InviteStatus

case class UserStatus(userId: String, isPlaying: Boolean, isWaiting: Boolean,
                      isIdle: Boolean, waitingOn: Option[String], playOn: Option[String],
                      isInvited: Boolean, inviteStatus: Option[InviteStatus])

object UserStatus {
  def idle(userId: String): UserStatus =
    apply(userId, isPlaying = false, isWaiting = false, isIdle = true, None, None, isInvited = false, None)

  def waiting(userId: String, waitingOn: String): UserStatus =
    apply(userId, isPlaying = false, isWaiting = true, isIdle = false, Some(waitingOn), None, isInvited = false, None)

  def playing(userId: String, playOn: String): UserStatus =
    apply(userId, isPlaying = true, isWaiting = false, isIdle = false, None, Some(playOn), isInvited = false, None)

  def invited(userId: String, inviteStatus: InviteStatus): UserStatus =
    apply(userId, isPlaying = false, isWaiting = false, isIdle = false, None, None, isInvited = true, Some(inviteStatus))

  object Idle {
    def unapply(u: UserStatus): Option[String] = if (u.isIdle) Some(u.userId) else None
  }

  object Waiting {
    def unapply(u: UserStatus): Option[(String, String)] =
      if(u.isWaiting) Some((u.userId, u.waitingOn.get)) else None
  }

  object Playing {
    def unapply(u: UserStatus): Option[(String, String)] =
      if(u.isPlaying) Some((u.userId, u.playOn.get)) else None
  }

  object Invited {
    def unapply(u: UserStatus): Option[(String, InviteStatus)] =
      if(u.isInvited) Some((u.userId, u.inviteStatus.get)) else None
  }

  sealed trait USWebSocket
  case class QueryUS() extends USWebSocket
  case class UpdateUS(userStatus: UserStatus) extends USWebSocket

  case class InviteStatus(user1: String, user2: String, rule: String)

  // the utilities of upickle json reader:
  import upickle.default._
  implicit val userStatusRW: ReadWriter[UserStatus] = macroRW[UserStatus]
  implicit val usWebSocketRW: ReadWriter[USWebSocket] =
    macroRW[QueryUS] merge macroRW[UpdateUS]
}