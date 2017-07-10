package com.fang.ajax

import com.fang._
import com.fang.data.AjaxResult
import com.fang.data.AjaxResult.AjaxResult
import org.scalajs.dom.ext.Ajax
import upickle.default.{read, write}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.annotation.JSExportTopLevel

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

  def defaultImageUrl(username: String): String =
    "http://identicon-1132.appspot.com/" + username

  def flickrRequest(query: String): String = {
    "https://api.flickr.com/services/rest/?method=flickr.photos.search&format=json&api_key=95a749a852b4e95944cca092880cecf3&text=" + query
  }

  def flickerSearchImages(query: String): Future[Seq[String]] = {
    Ajax.get(flickrRequest(query)).map{ xhr =>
      var text = xhr.responseText.replace("jsonFlickrApi(", "")
      text = text.substring(0, text.length - 1)
      val photoResult = read[PhotoResult](text)
      photoResult.photos.photo.map(item => item.getUrl)
    }
  }

  @JSExportTopLevel("searchPhoto")
  def searchPhoto(query: String): Unit = {
    flickerSearchImages(query).foreach(println(_))
  }

  case class PhotoItem(farm: Int, server: String, id: String, secret: String){
    def getUrl = s"https://farm$farm.staticflickr.com/$server/${id}_${secret}_s.jpg"
  }
  case class PhotoMiddle(photo: Seq[PhotoItem])
  case class PhotoResult(photos: PhotoMiddle)
}
