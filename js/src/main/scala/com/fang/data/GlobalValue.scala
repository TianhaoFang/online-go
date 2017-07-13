package com.fang.data

import com.fang.{ErrorMessage, UserSession, UserStatus}
import com.fang.ajax.UserAPI
import com.fang.ajax.UserStatusAPI.{ReceiveType, TypedUserStatusSocket}
import com.fang.data.AjaxResult.{Error, Ok}
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.{CloseEvent, Event, window}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.annotation.JSExportTopLevel

object GlobalValue {
  val userSession: Var[Option[UserSession]] = Var(None)
  var userStatusSession: Option[TypedUserStatusSocket] = None
  val userStatus: Var[Option[UserStatus]] = Var(None)
  val errorMessage: Var[Option[ErrorMessage]] = Var(None)
  val userEventListener: EventListener[ReceiveType] = new EventListener[ReceiveType]()
  val isAdmin: Var[Boolean] = Var(false)

  window.setInterval(() => updateUserSession(), 30000)
  userEventListener.addListener {
    case Left(a) =>
      errorMessage.value = Some(a)
      window.alert(a.message)
    case Right(b) =>
      userStatus.value = Some(b)
  }

  def updateUserSession(): Unit = {
    UserAPI.logStatus().foreach {
      case Ok(value) =>
        userSession.value = Some(value)
        if(value.role == UserSession.USER){
          isAdmin.value = false
          if(userStatusSession.isDefined){
            if(userStatusSession.get.userId != value.id){
              println(s"userStatusSession different ${userStatusSession.get.userId} ${value.id}")
              userStatusSession.get.close()
              userStatusSession = None
            }
          }
          if(userStatusSession.isDefined) {
          }else{
            userStatusSession = Some(new TypedUserStatusSocket(value.id) {
              override def onErrorMessage(message: String): Unit = {
                userEventListener.broadCast(Left(ErrorMessage(message)))
              }

              override def onData(userStatus: UserStatus): Unit = {
                userEventListener.broadCast(Right(userStatus))
              }

              override def onError(message: String): Unit = {
                userEventListener.broadCast(Left(ErrorMessage(message)))
              }

              override def onClose(event: CloseEvent): Unit = {
                userStatusSession = None
                println(s"connection to user status $userId is closed")
              }

              override def onOpen(event: Event): Unit = {
                println(s"new connection to user status $userId")
              }
            })
          }
        }else if(value.role == UserSession.ADMIN){
          isAdmin.value = true
          closeConnection()
        }
      case Error(_, _) =>
        closeConnection()
        userSession.value = None
    }
  }

  def closeConnection(): Unit = {
    userStatus.value = None
    errorMessage.value = None
    if(userStatusSession.isDefined){
      userStatusSession.get.close()
    }
  }
}
