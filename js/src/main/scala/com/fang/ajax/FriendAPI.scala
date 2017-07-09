package com.fang.ajax

import com.fang.{ErrorMessage, FriendModel}
import com.fang.data.AjaxResult
import com.fang.data.AjaxResult.AjaxResult
import org.scalajs.dom.ext.Ajax
import upickle.default.read

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FriendAPI {
  def getAllFriends(userId: String): Future[AjaxResult[Seq[FriendModel]]] =
    Ajax.get(s"/user/$userId/friends")
      .map(AjaxResult.mapToResult(read[Seq[FriendModel]]))
      .recover(AjaxResult.recovery)

  def getFriendById(userId: String, friendId: String): Future[AjaxResult[FriendModel]] =
    Ajax.get(s"/user/$userId/friends/$friendId")
      .map(AjaxResult.mapToResult(read[FriendModel]))
      .recover(AjaxResult.recovery)

  def deleteRelation(userId: String, friendId: String): Future[AjaxResult[String]] =
    Ajax.delete(s"/user/$userId/friends/$friendId")
      .map(AjaxResult.mapToResult(read[ErrorMessage]))
      .map(_.map(_.message))
      .recover(AjaxResult.recovery)

  def getInvented(userId: String): Future[AjaxResult[Seq[FriendModel]]] =
    Ajax.get(s"/user/$userId/invented")
      .map(AjaxResult.mapToResult(read[Seq[FriendModel]]))
      .recover(AjaxResult.recovery)

  def getInventedById(userId: String, friendId: String): Future[AjaxResult[FriendModel]] =
    Ajax.get(s"/user/$userId/invented/$friendId")
      .map(AjaxResult.mapToResult(read[FriendModel]))
      .recover(AjaxResult.recovery)

  def acceptInvention(userId: String, friendId: String): Future[AjaxResult[String]] =
    Ajax.put(s"/user/$userId/invented/$friendId")
      .map(AjaxResult.mapToResult(read[ErrorMessage]))
      .map(_.map(_.message))
      .recover(AjaxResult.recovery)
}
