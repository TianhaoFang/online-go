package com.fang.page

import com.fang.page.Page.Feedback
import com.thoughtworks.binding.Binding
import org.scalajs.dom.raw.Node

trait Page {
  def title(): String
  def onLoad(): Binding[Node]
  def onUnload(feedback: Feedback): Unit = {}
}

object Page {
  class Feedback{
    private var canceled = false
    def stopLeave(): Unit = {
      canceled = true
    }

    def proceedLeave(): Unit = {
      canceled = false
    }

    def isCanceled: Boolean = canceled
  }
}