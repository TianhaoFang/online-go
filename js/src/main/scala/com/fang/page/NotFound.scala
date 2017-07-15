package com.fang.page

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._

class NotFound(val path: String) extends Page {
  override def title(): String = "Not Found"

  @dom override def onLoad(): Binding[Node] = {
    <div>
      <h1 class="text-center">Not found path for {path}</h1>
      <a href="#login" class="btn btn-default btn-block">Home Page</a>
    </div>
  }
}