package com.fang.game

import org.scalatestplus.play.PlaySpec
import GameBoard._
import com.fang.game.rules.Reversi

class TestReversi extends PlaySpec {
  "Reversi.searchPiecePlaceFor" should {
    "return a false" in {
      val gameBoard = GameBoard(8)
      // set to a particular status that black cannot move
      for(j <- 0 to 5; i <- 0 to 7) gameBoard.set(i, j)(BLACK)
      for(j <- 6 to 7; i <- 2 to 7) gameBoard.set(i, j)(BLACK)
      for(j <- 3 to 4; i <- 0 to 6) gameBoard.set(i, j)(WHITE)
      gameBoard.set(2, 2)(WHITE)
      gameBoard.set(4, 2)(WHITE)
      gameBoard.set(3, 0)(BLACK)
      gameBoard.set(3, 2)(BLACK)
      gameBoard.set(3, 6)(BLACK)
      gameBoard.set(5, 5)(WHITE)
      gameBoard.set(6, 5)(WHITE)
      gameBoard.set(6, 6)(WHITE)
      gameBoard.set(6, 7)(WHITE)
      gameBoard.set(3, 7)(EMPTY)

      Reversi.searchPiecePlaceFor(BLACK, gameBoard) mustBe false
      gameBoard.set(2, 5)(WHITE)
      Reversi.searchPiecePlaceFor(BLACK, gameBoard) mustBe true
    }
  }
}
