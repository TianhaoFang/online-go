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
  val ignoreHandle: (Int, Int) => Unit = (_, _) => {}
  val pieceR: Int = 18
  val gridSize: Int = 40
  val PI2: Double = Math.PI * 2

  def canvasWidth(size: Int): Int = 2 * pieceR + gridSize * (size - 1)

  def coordinate(rowNum: Int): Int = pieceR + gridSize * rowNum

  def findColumn(canvasX: Int): Int = {
    (canvasX + gridSize / 2 - pieceR) / gridSize
  }

  @dom def apply
  (
    gameBoard: Binding[(GameBoard, Int)],
    afterPaint: Ctx2D => Unit = noop,
    onClick: (Int, Int) => Unit = ignoreHandle
  ): Binding[Node] = {
    val localValue = BoardCanvas((x, y) => onClickHandle(x, y, onClick))
    drawBoard(localValue.bind, gameBoard.bind._1, afterPaint)
    localValue.bind
  }

  @dom def drawBoard(canvas: HTMLCanvasElement, gameBoard: GameBoard, afterPaint: Ctx2D => Unit): Unit = {
    val size = gameBoard.boardSize
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
          fillPieceCircle(ctx, x, y)
          ctx.stroke()
          ctx.fill()
        }
    }
    afterPaint(ctx)
  }

  def fillPieceCircle(context: Ctx2D, x: Int, y: Int): Unit = {
    val cx = coordinate(x)
    val cy = coordinate(y)
    context.moveTo(cx, cy)
    context.arc(cx, cy, pieceR, 0, PI2)
  }

  @dom def onClickHandle(canvasX: Int, canvasY: Int, handle: (Int, Int) => Unit): Unit = {
    val x = findColumn(canvasX)
    val y = findColumn(canvasY)
    handle(x, y)
  }
}
