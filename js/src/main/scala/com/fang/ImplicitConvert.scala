package com.fang

import com.thoughtworks.binding.Binding

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContextExecutor

object ImplicitConvert {
  implicit def makeIntellijHappy(x: scala.xml.Node): Binding[org.scalajs.dom.raw.Node] = ???
  implicit def makeIntellijHappy2[T](x: T): Binding[T] = ???
  implicit val executionContext: ExecutionContextExecutor = global
}
