package com.fang.page

import com.fang.ImplicitConvert._
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, HTMLInputElement}

object DomUtil {
  def bindInputValue(e: Event, binding: Var[String]): Unit = {
    val text: String = e.target.asInstanceOf[HTMLInputElement].value.trim()
    binding.value = text
  }

  def bindInputValue[T](e: Event, binding: Var[T], mapper: String => T): Unit = {
    val text: String = e.target.asInstanceOf[HTMLInputElement].value.trim()
    binding.value = mapper(text)
  }

  import java.util.regex.Pattern

  private val ptr: Pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

  @dom def validEmail(emailStr: Binding[String]): Binding[Boolean] = {
    val text: String = emailStr.bind
    ptr.matcher(text).matches()
  }

  @dom def showClassIf(prev: String, toShow: String, show: Binding[Boolean]): Binding[String] = {
    if(show.bind){
      prev + " " + toShow
    }else{
      prev
    }
  }

  def hideClassIf(prev: String, toHide: String, hide: Binding[Boolean]): Binding[String] = {
    @dom val show: Binding[Boolean] = !hide.bind
    showClassIf(prev, toHide, show)
  }
}
