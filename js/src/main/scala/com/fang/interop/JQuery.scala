package com.fang.interop

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope

@js.native
@JSGlobalScope
object JQuery extends js.Object {
  // add the closing functionality by using the jQuery facade from scala
  def $(selector: String): ModalElement = js.native

  @js.native
  trait ModalElement extends js.Object {
    def modal(command: String):Unit = js.native
  }
}
