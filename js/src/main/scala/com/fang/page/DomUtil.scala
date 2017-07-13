package com.fang.page

import com.fang.ImplicitConvert._
import com.thoughtworks.binding.Binding.{Var, Vars}
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

  def bindCheckbox(e: Event, binding: Var[Boolean]): Unit = {
    val bool: Boolean = e.target.asInstanceOf[HTMLInputElement].checked
    binding.value = bool
  }

  import java.util.regex.Pattern

  private val ptr: Pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

  @dom def validEmail(emailStr: Binding[String]): Binding[Boolean] = {
    val text: String = emailStr.bind
    ptr.matcher(text).matches()
  }

  def showClassIf(prev: String, toShow: String, show: Boolean): String = {
    if(show){
      prev + " " + toShow
    }else{
      prev
    }
  }

  def hideClassIf(prev: String, toHide: String, hide: Boolean): String = {
    showClassIf(prev, toHide, !hide)
  }

  def assignVars[T](vars: Vars[T], seq: Seq[T]): Unit = {
    val pairs: Seq[(T, Int)] = seq.zipWithIndex
    val proxy = vars.value
    pairs.foreach{
      case (t, index) =>
        if(index < proxy.length){
          proxy.update(index, t)
        }else{
          proxy.append(t)
        }
    }
    for(i <- Range(pairs.length, proxy.length).reverse){
      proxy.remove(i)
    }
  }
}
