package com.fang.game

import upickle.default._

case class Step(x: Int, y: Int){
  def toInt:Int = x * 100 + y
}

object Step{
  implicit val wrapper:ReadWriter[Step] = macroRW[Step]

  def fromInt(int: Int): Step = {
    val tx: Int = int / 100
    val ty: Int = int - 100 * tx
    Step(tx, ty)
  }
}
