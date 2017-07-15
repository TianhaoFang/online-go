package com.fang.segment

import com.fang.game.{GameBoard, Step}
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs
import org.scalajs.dom.raw.{HTMLCanvasElement, Node}
import com.fang.ImplicitConvert._

object BasicBoard {
  type Ctx2D = scalajs.dom.CanvasRenderingContext2D

  val noop: Ctx2D => Unit = (_: Ctx2D) => {}
  val pieceR: Int = 18
  val gridSize: Int = 40
  val PI2: Double = Math.PI * 2

  def canvasWidth(size: Int): Int = 2 * pieceR + gridSize * (size - 1)

  def coordinate(rowNum: Int): Int = pieceR + gridSize * rowNum

  @dom def apply
  (
    ref: Var[HTMLCanvasElement],
    gameBoard: Binding[(GameBoard, Int)],
    afterPaint: Ctx2D => Unit = noop
  ): Binding[Node] = {
    val localValue = BoardCanvas(ref)
    drawBoard(localValue.bind, gameBoard.bind._1, afterPaint)
    localValue.bind
  }

  @dom def drawBoard(canvas: HTMLCanvasElement, gameBoard: GameBoard, afterPaint: Ctx2D => Unit): Unit = {
    val size = gameBoard.boardSize
    println("drawBoard is called")
    val ctx = canvas.getContext("2d").asInstanceOf[Ctx2D]
    if (canvas.height != canvasWidth(size)) {
      canvas.height = canvasWidth(size)
      canvas.width = canvasWidth(size)
    }
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = "black"
    for (i <- Range(0, size)) {
      ctx.beginPath()
      ctx.moveTo(coordinate(0), coordinate(i))
      ctx.lineTo(coordinate(size - 1), coordinate(i))
      ctx.stroke()

      ctx.beginPath()
      ctx.moveTo(coordinate(i), coordinate(0))
      ctx.lineTo(coordinate(i), coordinate(size - 1))
      ctx.stroke()
    }
    gameBoard.foreach{
      case (Step(x, y), piece) =>
        if(piece == GameBoard.BLACK) ctx.fillStyle = "black"
        else if(piece == GameBoard.WHITE) ctx.fillStyle = "white"
        if(piece != GameBoard.EMPTY){
          ctx.beginPath()
          ctx.moveTo(coordinate(x), coordinate(y))
          ctx.arc(coordinate(x), coordinate(y), pieceR, 0, PI2)
          ctx.stroke()
          ctx.fill()
        }
    }
    afterPaint(ctx)
  }
}
