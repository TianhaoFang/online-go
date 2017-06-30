package com.fang.ajax

import com.fang.{ErrorMessage, UserModel}
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

  def createUser(userModel: UserModel): Future[AjaxResult[String]] =
    Ajax.post("/user", write(userModel))
    .map(AjaxResult.mapToResult(read[ErrorMessage](_).message))

  def updateUser(userModel: UserModel.NoPassword): Future[AjaxResult[UserModel.NoPassword]] =
    Ajax.put(s"/user/${userModel.username}", write(userModel))
    .map(AjaxResult.mapToResult(read[UserModel.NoPassword]))
}
