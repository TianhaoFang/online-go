package com.fang.game

class GameBoard private(val boardSize: Int, private val array: Array[Int])
  extends Traversable[(Step, Int)] {
  private var listeners:Seq[(Step, Int) => Any] = Seq()

  def outBoundStr(p: Int, s: String = ""): String =
    s"$s: $p is out of boundary of length $boardSize"

  private def inBoundary(p: Int, s: String): Unit = {
    if (0 <= p && p <= boardSize - 1) {}
    else throw new ArrayIndexOutOfBoundsException(outBoundStr(p, s))
  }

  def inBoundary(x: Int, y: Int): Unit = {
    inBoundary(x, "x")
    inBoundary(y, "y")
  }

  private def rightPiece(piece: Int): Unit = {
    if (piece <= 2 && piece >= 0) {}
    else GameBoard.wrongPiece
  }

  private def at(x: Int, y: Int): Int = y * boardSize + x

  def get(x: Int, y: Int): Int = {
    inBoundary(x, y)
    array(at(x, y))
  }

  def set(x: Int, y: Int)(piece: Int): Int = {
    inBoundary(x, y)
    rightPiece(piece)
    if(piece == array(at(x, y))) return piece
    array(at(x, y)) = piece
    val step = Step(x, y)
    listeners.foreach(_(step, piece))
    piece
  }

  def update(x: Int, y: Int)(mapper: Int => Int): Int = {
    val oldPiece = get(x, y)
    val newPiece = mapper(oldPiece)
    rightPiece(newPiece)
    if(oldPiece == newPiece) return oldPiece
    array(at(x, y)) = newPiece
    val step = Step(x, y)
    listeners.foreach(_(step, newPiece))
    newPiece
  }

  def copy(): GameBoard = {
    val result = GameBoard(boardSize)
    Array.copy(array, 0, result.array, 0, array.length)
    result
  }

  override def foreach[U](f: ((Step, Int)) => U): Unit = {
    for(j <- Range(0, boardSize); i <- Range(0, boardSize)){
      f((Step(i, j), array(at(i, j))))
    }
  }

  def removeListener(f: (Step, Int) => Any): Unit = listeners = listeners.filterNot(_ == f)
  def addLister(f: (Step, Int) => Any):Unit = {
    removeListener(f)
    listeners = listeners ++ Seq(f)
  }

  override def toString(): String = {
    val builder = new StringBuilder()
    for(j <- Range(0, boardSize)){
      builder.append('\n')
      for(i <- Range(0, boardSize)){
        builder.append(array(at(i, j)) match {
          case GameBoard.BLACK => "X"
          case GameBoard.WRITE => "O"
          case GameBoard.EMPTY => " "
        }).append("|")
      }
    }
    builder.toString()
  }
}

object GameBoard {
  def apply(size: Int) = new GameBoard(size, new Array[Int](size * size))

  def apply(gameBoard: GameBoard) = new GameBoard(gameBoard.boardSize, gameBoard.array)

  def wrongPiece = throw new IllegalStateException(s"piece should be none, write or black")

  val BLACK = 1
  val EMPTY = 0
  val WRITE = 2

  def getColorByStep(step: Int): Int = {
    (step % 2) + 1
  }

  def oppositeColor(color: Int): Int = color match {
    case EMPTY => EMPTY
    case BLACK => WRITE
    case WRITE => BLACK
  }
}
