package com.fang.segment

import com.fang.data.GlobalValue
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{HTMLCanvasElement, MouseEvent, Node}
import com.fang.ImplicitConvert._
import com.fang.Main
import org.scalajs.dom.window

object BoardCanvas {
  val ignored: (Int, Int) => Unit = (_, _) => {}

  def apply(handle: (Int, Int) => Unit = ignored): Binding[HTMLCanvasElement] = {
    innerApply(handle).asInstanceOf[Binding[HTMLCanvasElement]]
  }

  @dom def innerApply(handle: (Int, Int) => Unit): Binding[Node] = {
    window.setTimeout(() => {
      GlobalValue.windowWidth.value = 800
      GlobalValue.windowHeight.value = 900
      Main.resetHW()
    }, 0)

    <canvas class="gameBoard" style={GlobalValue.boardStyle.bind}
            onclick={onclickHandle(_:MouseEvent, handle)}>
      canvas is not supported in this bowser
    </canvas>
  }

  @dom def onclickHandle(event: MouseEvent, handle: (Int, Int) => Unit): Unit = {
    val canvas = event.target.asInstanceOf[HTMLCanvasElement]
    val rect = canvas.getBoundingClientRect()
    val ax = event.clientX - rect.left
    val ay = event.clientY - rect.top

    val absSize = GlobalValue.sizeForBoard.bind
    val canvasSize = canvas.width
    val rx = ax * canvasSize / absSize
    val ry = ay * canvasSize / absSize
    handle(rx.toInt, ry.toInt)
  }
}
