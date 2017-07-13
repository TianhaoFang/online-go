package com.fang.page
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}
import com.fang.ImplicitConvert._
import com.fang.UserStatus
import com.fang.UserStatus.UpdateUS
import com.fang.data.GlobalValue
import com.fang.segment.UserStatusNavBar
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.window

class WaitingPage(rule: String, userId: String) extends Page{
  val process: Var[Int] = Var(0)
  val interval: Int = window.setInterval(() => {
    val amount = (Math.random() * 10).toInt
    process.value += amount
    if(process.value > 100) process.value = process.value - 100
  }, 500L)
  @dom val percent: Binding[String] = "" + process.bind + "%"

  override def title(): String = "Waiting"

  @dom override def onLoad(): Binding[Node] = {
    println("enter WaitingPage.onLoad")

    <div>
      {UserStatusNavBar().bind}

      <div class="container with-padding">
        <h1><b>Waiting For {rule}</b></h1>
        <div class="padding10"></div>
        <div class="progress">
          <div class="progress-bar progress-bar-success progress-bar-striped active" style={s"width: ${percent.bind};"}>
            {percent.bind}
          </div>
        </div>
        <button class="btn btn-danger btn-block" onclick={_:Event => toggleCancel()}>Cancel</button>
      </div>
    </div>
  }

  override def onUnload(feedback: Page.Feedback): Unit = {
    println("leaving WaitingPage.onUnload")
    window.clearInterval(interval)
  }

  def toggleCancel(): Unit = {
    GlobalValue.userStatusSession match {
      case None =>
      case Some(x) =>
        x.sendMessage(UpdateUS(UserStatus.idle(userId)))
    }
  }
}
