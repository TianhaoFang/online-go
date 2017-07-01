package com.fang.game.rules

import com.fang.game.GameRule

import scala.collection.immutable.HashMap

object Rules {
  private def pair(gameRule: GameRule) = (gameRule.name, gameRule)

  val gomoku = new Gomoku

  private val map: HashMap[String, GameRule] = HashMap(
    pair(gomoku)
  )

  def apply(key: String): GameRule = map.get(key) match {
    case Some(gameRule) => gameRule
    case None => throw new NoSuchElementException(s"not find gameRule for $key")
  }
}
