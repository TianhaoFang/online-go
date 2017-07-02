package com.fang.game.rules

import com.fang.game.GameBoard.EMPTY
import com.fang.game.{GameBoard, GameRule, Status}

class Reversi extends GameRule {
  @inline
  def isSuchColor(gameBoard: GameBoard, color: Int, x: Int, y: Int): Boolean =
    x < 8 && x >= 0 && y < 8 && y >= 0 && gameBoard.get(x, y) == color

  override def canPlace(gameBoard: GameBoard)(x: Int, y: Int, step: Int): Boolean = {
    val color = GameBoard.getColorByStep(step)
    val oppo = GameBoard.oppositeColor(color)
    if (gameBoard.get(x, y) != EMPTY) return false
    var (nx, ny) = (0, 0)
    for (i <- Reversi.fullSurround.indices) {
      val (dx, dy) = Reversi.fullSurround(i)
      nx = x + dx
      ny = y + dy
      if (isSuchColor(gameBoard, oppo, nx, ny)) {
        while (isSuchColor(gameBoard, oppo, nx, ny)) {
          nx += dx
          ny += dy
        }
        if (isSuchColor(gameBoard, color, nx, ny)) return true
      }
    }
    false
  }

  override def placeOn(gameBoard: GameBoard)(x: Int, y: Int, step: Int): Status = {
    val color = GameBoard.getColorByStep(step)
    val oppo = GameBoard.oppositeColor(color)
    var nx = 0
    var ny = 0
    gameBoard.set(x, y)(color)

    // first update the pieces in lines or in corners can be reversed
    for (i <- Reversi.fullSurround.indices) {
      val (dx, dy) = Reversi.fullSurround(i)
      nx = x + dx
      ny = y + dy
      if (isSuchColor(gameBoard, oppo, nx, ny)) {
        while (isSuchColor(gameBoard, oppo, nx, ny)) {
          nx += dx
          ny += dy
        }
        if (isSuchColor(gameBoard, color, nx, ny)) {
          nx -= dx
          ny -= dy
          while (isSuchColor(gameBoard, oppo, nx, ny)){
            gameBoard.set(nx, ny)(color)
            nx -= dx
            ny -= dy
          }
        }
      }
    }

    // then search and find the valid next step for the opposite
    if(Reversi.searchPiecePlaceFor(oppo, gameBoard)) Status.Continue()
    else if(Reversi.searchPiecePlaceFor(color, gameBoard)) Status.SkipNext()
    else {
      var blackCount = 0
      var whiteCount = 0
      gameBoard.foreach(_._2 match {
        case GameBoard.BLACK => blackCount += 1
        case GameBoard.WHITE => whiteCount += 1
        case _ =>
      })
      if(blackCount > whiteCount) Status.BlackWin() else Status.WriteWin()
    }
  }

  override def size: Int = 8

  override def name: String = "reversi"

  override def init(gameBoard: GameBoard): Unit = {
    super.init(gameBoard)
    gameBoard.set(3, 3)(GameBoard.WHITE)
    gameBoard.set(3, 4)(GameBoard.BLACK)
    gameBoard.set(4, 3)(GameBoard.BLACK)
    gameBoard.set(4, 4)(GameBoard.WHITE)
  }
}

object Reversi {
  private val fullSurround: Array[(Int, Int)] = Array(
    (-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1)
  )

  @inline
  def inBoard(x: Int, y: Int): Boolean = x < 8 && x >= 0 && y < 8 && y > 0

  def searchPiecePlaceFor(color: Int, gameBoard: GameBoard): Boolean = {
    if (color == EMPTY) return false
    val oppo = GameBoard.oppositeColor(color)

    def lineSearch: Int => Boolean = {
      var stage = 0
      return { c: Int => {
        if (c == EMPTY) {
          stage = 1
          false
        }
        else if ((stage == 1 || stage == 2) && c == oppo) {
          stage = 2
          false
        }
        else if (stage == 2 && c == color) true
        else {
          stage = 0
          false
        }
      }
      }
    }

    // first test from left to right, from top to down
    for (j <- Range(0, 8)) {
      var lineSearcher: (Int) => Boolean = lineSearch
      var rowSearcher: (Int) => Boolean = lineSearch
      // the left right line path and top down search
      for (i <- Range(0, 8)) {
        if (lineSearcher(gameBoard.get(i, j))) return true
        if (rowSearcher(gameBoard.get(j, i))) return true
      }
      // then right left search and down top search
      lineSearcher = lineSearch
      rowSearcher = lineSearch
      for (i <- Range(0, 8).reverse) {
        if (lineSearcher(gameBoard.get(i, j))) return true
        if (rowSearcher(gameBoard.get(j, i))) return true
      }
    }

    // from left top to right down
    for (j <- Range(0, 6)) {
      val searcher1 = lineSearch
      val searcher2 = lineSearch
      var x = j
      var y = 0
      while (inBoard(x, y)) {
        if (searcher1(gameBoard.get(x, y))) return true
        if (searcher2(gameBoard.get(y, x))) return true
        x += 1
        y += 1
      }
    }

    // from right down to left top
    for (j <- Range(2, 8)) {
      val searcher1 = lineSearch
      val searcher2 = lineSearch
      var x = 7
      var y = j
      while (inBoard(x, y)) {
        if (searcher1(gameBoard.get(x, y))) return true
        if (searcher2(gameBoard.get(y, x))) return true
        x -= 1
        y -= 1
      }
    }

    // from right top to bottom down
    for (j <- Range(0, 6)) {
      val searcher1 = lineSearch
      val searcher2 = lineSearch
      var x = 7
      var y = j
      while (inBoard(x, y)) {
        if (searcher1(gameBoard.get(x, y))) return true
        if (searcher2(gameBoard.get(7 - y, 7 - x))) return true
        x -= 1
        y += 1
      }
    }

    // from bottom down to right top
    for (j <- Range(2, 8)) {
      val searcher1 = lineSearch
      var searcher2 = lineSearch
      var x = 0
      var y = j
      while (inBoard(x, y)) {
        if (searcher1(gameBoard.get(x, y))) return true
        if (searcher2(gameBoard.get(7 - y, 7 - x))) return true
        x += 1
        y -= 1
      }
    }
    false
  }
}