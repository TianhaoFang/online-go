package com.fang.page
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._
import com.fang.segment.UserStatusNavBar

class FriendPage(val userId: String) extends Page{
  override def title(): String = "Friends"

  @dom override def onLoad(): Binding[Node] = {
    <div>
      {UserStatusNavBar().bind}


    </div>
  }
}
