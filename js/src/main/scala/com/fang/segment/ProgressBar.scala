package com.fang.segment

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._

object ProgressBar {
  @dom def apply(current: Int, total: Int): Binding[Node] = {
    val percentage: Int = if(total > 0) current * 100 / total else 0
    val style: String = "width: " + percentage + "%;"
    val text: String = s"$current / $total"

    <div class="progress">
      <div class="progress-bar progress-bar-striped" style={style}>
        {text}
      </div>
      <div class="progress-black">{text}</div>
    </div>
  }
}
