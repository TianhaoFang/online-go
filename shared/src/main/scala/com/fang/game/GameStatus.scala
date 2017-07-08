package com.fang.game

import com.fang.game.rules.Rules

import scala.util.{Failure, Success, Try}

class GameStatus(val gameRule: GameRule) extends Traversable[(Step, Int)] {
  private val gameBoard: GameBoard = GameBoard(gameRule.size)
  private var error: Option[Exception] = None
  private var status: Status = Status.Continue()
  private var nextStep: Int = 0

  gameRule.init(gameBoard)

  def getError: Option[Exception] = error
  def getStatus: Status = status
  def getGameBoard: GameBoard = gameBoard

  def step: Int = nextStep - 1
  def put(piece: Step, step: Int):Either[String, Status] = {
    val (x, y) = (piece.x, piece.y)
    if(x >= gameBoard.boardSize || x < 0)
      return Left(gameBoard.outBoundStr(x, "x"))
    if(y >= gameBoard.boardSize || y < 0)
      return Left(gameBoard.outBoundStr(y, "y"))
    if(error.isDefined) return Left(error.get.getMessage)
    if(nextStep != step) return Left(s"wrong step, expected: $nextStep, actual: $step")
    if(status == Status.SkipNext()){
      status = Status.Continue()
      nextStep += 1
      return Right(status)
    }
    if(status != Status.Continue())
      return Left(s"the game is not running, is finished")
    if(gameBoard.get(x, y) != GameBoard.EMPTY)
      return Left(s"the space($x, $y) is already occupied by other piece")
    if(!gameRule.canPlace(gameBoard)(x, y, step))
      return Left(s"this space($x, $y) is not allowed by game rule")
    Try(gameRule.placeOn(gameBoard)(x, y, step)) match {
      case Failure(exception) =>
        error = Some(exception.asInstanceOf[Exception])
        Left(exception.getMessage)
      case Success(newStatus) =>
        status = newStatus
        nextStep += 1
        Right(newStatus)
    }
  }

  override def foreach[U](f: ((Step, Int)) => U): Unit = gameBoard.foreach(f)

  override def toString(): String = {
    s"GameStatus(status = $status, step = $step, gameBoard = $gameBoard)"
  }
}

object GameStatus {
  def apply(gameRule: GameRule) = new GameStatus(gameRule)
  def apply(ruleName: String) = new GameStatus(Rules(ruleName))
  def apply(ruleName: String, past: Seq[Step]): GameStatus = {
    val result = apply(ruleName)
    val steps: Seq[(Step, Int)] = past.zipWithIndex
    steps.foreach(t => result.put(t._1, t._2))
    result
  }
}

