package webSocket

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Concurrent.Channel

import scala.collection.mutable

class KeyBroadCastActor extends Actor {
  private val hashMap: mutable.Map[String, mutable.HashSet[Channel[String]]] =
    new mutable.HashMap[String, mutable.HashSet[Channel[String]]]()

  override def receive: Receive = {
    case command: KBCommand => command match {
      case AddOutput(key, out) => addOutput(key, out)
      case RemoveOutput(key, out) => removeOutput(key, out)
      case BoardCast(key, onEach) => boardCast(key, onEach)
    }
    case _ =>
      println(hashMap)
  }

  def addOutput(key: String, out: Channel[String]): Unit = {
    val set = hashMap.getOrElseUpdate(key, mutable.HashSet())
    set += out
  }
  def removeOutput(key: String, out: Channel[String]):Unit = {
    hashMap.get(key) match {
      case None =>
      case Some(set) =>
        set -= out
        if(set.isEmpty) hashMap.remove(key)
    }
  }
  def boardCast(key: String, onEach: Channel[String] => Unit):Unit = {
    hashMap.get(key) match {
      case None =>
      case Some(set) =>
        set.foreach(onEach)
    }
  }
}