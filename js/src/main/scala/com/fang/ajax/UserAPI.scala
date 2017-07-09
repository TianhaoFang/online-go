package com.fang.ajax

import com.fang._
import com.fang.data.AjaxResult
import com.fang.data.AjaxResult.AjaxResult
import org.scalajs.dom.ext.Ajax
import upickle.default.{read, write}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserAPI {
  def getUser(id: String): Future[AjaxResult[Map[String, String]]] =
    Ajax.get(s"/user/$id")
      .map(AjaxResult.mapToResult(read[Map[String, String]]))
      .recover(AjaxResult.recovery)

  def createUser(userModel: UserModel): Future[AjaxResult[String]] =
    Ajax.post("/user", write(userModel))
      .map(AjaxResult.mapToResult(read[ErrorMessage](_).message))
      .recover(AjaxResult.recovery)

  def updateUser(userModel: UserModel.NoPassword): Future[AjaxResult[UserModel.NoPassword]] =
    Ajax.put(s"/user/${userModel.username}", write(userModel))
      .map(AjaxResult.mapToResult(read[UserModel.NoPassword]))
      .recover(AjaxResult.recovery)

  def updatePassword(id: String, password: Password): Future[AjaxResult[String]] =
    Ajax.put(s"/user/$id/password", write(password))
      .map(AjaxResult.mapToResult(read[ErrorMessage](_).message))
      .recover(AjaxResult.recovery)

  def userLogin(id: String, password: String): Future[AjaxResult[UserSession]] =
    Ajax.post("/user/login", write(LoginRequest(id, password)))
      .map(AjaxResult.mapToResult(read[UserSession]))
      .recover(AjaxResult.recovery)

  def logout(): Future[AjaxResult[String]] =
    Ajax.delete("/login")
      .map(AjaxResult.mapToResult(read[ErrorMessage](_).message))
      .recover(AjaxResult.recovery)

  def logStatus(): Future[AjaxResult[UserSession]] =
    Ajax.get("/login")
      .map(AjaxResult.mapToResult(read[UserSession]))
      .recover(AjaxResult.recovery)

  def adminLogin(id: String, password: String): Future[AjaxResult[UserSession]] =
    Ajax.post("/admin/login", write(LoginRequest(id, password)))
      .map(AjaxResult.mapToResult(read[UserSession]))
      .recover(AjaxResult.recovery)

  def updateAdminPassword(password: Password): Future[AjaxResult[String]] =
    Ajax.put("/admin/password", write(password))
      .map(AjaxResult.mapToResult(read[ErrorMessage](_).message))
      .recover(AjaxResult.recovery)
}
