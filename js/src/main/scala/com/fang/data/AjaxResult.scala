package com.fang.data

import com.fang.ErrorMessage
import org.scalajs.dom.ext.AjaxException
import org.scalajs.dom.raw.XMLHttpRequest
import upickle.default.read

object AjaxResult {

  sealed trait AjaxResult[T] {
    def map[R](mapper: T => R): AjaxResult[R]
  }

  case class Ok[T](value: T) extends AjaxResult[T] {
    override def map[R](mapper: (T) => R): AjaxResult[R] = Ok(mapper(value))
  }

  case class Error[T](message: String, code: Int) extends AjaxResult[T] {
    override def map[R](mapper: (T) => R): AjaxResult[R] = Error(message, code)
  }

  def mapToResult[T](mapper: String => T): XMLHttpRequest => AjaxResult[T] = { xhr =>
    println("mapToResult is called"+  xhr.status + " " + xhr.responseText)
    val code = xhr.status
    if (code >= 200 && code <= 299) {
      Ok(mapper(xhr.responseText))
    } else {
      val errorMessage = read[ErrorMessage](xhr.responseText)
      Error(errorMessage.message, xhr.status)
    }
  }

  def recovery[U]:PartialFunction[Throwable, AjaxResult[U]] = {
    case AjaxException(xhr) =>
      val errorMessage = read[ErrorMessage](xhr.responseText)
      Error[U](errorMessage.message, xhr.status)
  }
}
