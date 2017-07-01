package com.fang.game

trait GameRule {
  def canPlace(gameBoard: GameBoard)(x: Int, y: Int, step: Int): Boolean
  def placeOn(gameBoard: GameBoard)(x: Int, y: Int, step: Int): Status
  def size: Int
  def init(gameBoard: GameBoard):Unit = {}
  def name: String
}
