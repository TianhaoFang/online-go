package com.fang

import com.thoughtworks.binding.Binding.{BindingInstances, Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{Event, Node}

import scala.scalajs.js.JSApp

object Main extends JSApp{
  implicit def makeIntellijHappy(x: scala.xml.Node): Binding[org.scalajs.dom.raw.Node] = ???
  implicit def makeIntellijHappy2[T](x: T): Binding[T] = ???

  override def main(): Unit = {
    println("it works")
    println(upickle.default.write(Test.User("sdada", 8)))
    dom.render(org.scalajs.dom.document.getElementById("app"), render)
  }

  @dom
  def render: Binding[Node] = {
    val index:Var[Int] = Var(5)
    val array:Var[Seq[Int]] = Var(Seq(1, 2, 3, 4))
    val list = Vars("").flatMapBinding(ignore => BindingInstances.map(array)(seq => Vars(seq: _*)))

    <div>
      { for(int <- list) yield {<div>{int.toString}</div>}}
      <button onclick={ event:Event => {
        index.value = index.value + 1
        array.value ++= Seq(index.value)
      }}>{index.bind.toString}</button>
      <div>{array.bind.toString()}</div>
    </div>
  }
}
