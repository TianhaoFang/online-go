package com.fang.data

import scala.collection.mutable

class EventListener[T] {
  private val set: mutable.HashSet[T => Unit] = mutable.HashSet()

  def addListener(handle: T => Unit): Unit = {
    set += handle
  }

  def removeListener(handle: T => Unit): Unit = {
    set -= handle
  }

  def broadCast(event: T): Int = {
    var result = 0
    for (elem <- set) {
      elem(event)
      result += 1
    }
    result
  }
}
