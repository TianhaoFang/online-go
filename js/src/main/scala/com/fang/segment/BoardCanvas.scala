package com.fang.segment

import com.fang.data.GlobalValue
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{HTMLCanvasElement, Node}
import com.fang.ImplicitConvert._
import com.fang.Main
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.window

object BoardCanvas {
  def apply(ref: Var[HTMLCanvasElement], ignored: Binding[String] = Var("")): Binding[HTMLCanvasElement] = {
    innerApply(ref, ignored).asInstanceOf[Binding[HTMLCanvasElement]]
  }

  @dom def innerApply(ref: Var[HTMLCanvasElement], ignored: Binding[String]): Binding[Node] = {
    window.setTimeout(() => {
      GlobalValue.windowWidth.value = 800
      GlobalValue.windowHeight.value = 900
      Main.resetHW()
    }, 0)

    <canvas class="gameBoard" data:data-my-data={ignored.bind}
            style={GlobalValue.boardStyle.bind}>
      canvas is not supported in this bowser
    </canvas>
  }
}
