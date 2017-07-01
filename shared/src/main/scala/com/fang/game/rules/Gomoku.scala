package com.fang.game.rules

import com.fang.game.{GameBoard, GameRule, Status}

class Gomoku extends GameRule {
  override def name: String = "gomoku"

  override def canPlace(gameBoard: GameBoard)(x: Int, y: Int, step: Int): Boolean = {
    true
  }

  override def placeOn(gameBoard: GameBoard)(x: Int, y: Int, step: Int): Status = {
    val color = GameBoard.getColorByStep(step)
    gameBoard.set(x, y)(color)
    val inLine = pieceNumInLine(gameBoard, x, y) _
    // now judge weather any of the five row are in a line
    val win = inLine(+1, 0) + inLine(-1, 0) - 1 >= 5 ||
      inLine(0, +1) + inLine(0, -1) - 1 >= 5 ||
      inLine(+1, +1) + inLine(-1, -1) - 1 >= 5 ||
      inLine(+1, -1) + inLine(-1, +1) - 1 >= 5
    if (win) color match {
      case GameBoard.BLACK => Status.BlackWin()
      case GameBoard.WRITE => Status.WriteWin()
    } else {
      Status.Continue()
    }
  }

  override def size: Int = 15

  def inBoard(p: Int): Boolean = p < size && p >= 0

  def pieceNumInLine(gameBoard: GameBoard, startX: Int, startY: Int)
                    (deltaX: Int, deltaY: Int): Int = {
    val color = gameBoard.get(startX, startY)
    if (color == GameBoard.EMPTY) return 0
    var (x, y) = (startX, startY)
    var result = 1
    while (true) {
      x += deltaX
      y += deltaY
      if (!inBoard(x) || !inBoard(y)) return result
      if (color != gameBoard.get(x, y)) return result
      result += 1
    }
    throw new RuntimeException("should not reach there")
  }
}
