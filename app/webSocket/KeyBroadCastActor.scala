package webSocket

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.HOURS

class KeyBroadCastActor extends Actor {
  private val hashMap: mutable.Map[String, mutable.HashSet[WebSocketProcessor[String, String]]] =
    new mutable.HashMap[String, mutable.HashSet[WebSocketProcessor[String, String]]]()

  override def receive: Receive = {
    case command: KBCommand => command match {
      case AddOutput(key, out) => addOutput(key, out)
      case RemoveOutput(key, out) => removeOutput(key, out)
      case BoardCast(key, onEach) => boardCast(key, onEach)
      case GetSize(key) => querySize(key)
    }
    case _ =>
      println(hashMap)
  }

  def addOutput(key: String, out: WebSocketProcessor[String, String]): Unit = {
    val set = hashMap.getOrElseUpdate(key, mutable.HashSet())
    set += out
  }
  def removeOutput(key: String, out: WebSocketProcessor[String, String]):Unit = {
    hashMap.get(key) match {
      case None =>
      case Some(set) =>
        set -= out
        if(set.isEmpty) hashMap.remove(key)
    }
  }
  def boardCast(key: String, onEach: WebSocketProcessor[String, String] => Unit):Unit = {
    hashMap.get(key) match {
      case None =>
      case Some(set) =>
        set.foreach(onEach)
    }
  }
  def querySize(key: String):Unit = {
    val result = hashMap.get(key) match {
      case None => 0
      case Some(set) => set.size
    }
    sender() ! result
  }
}

object KeyBroadCastActor {
  implicit val timeout = akka.util.Timeout(5, HOURS)

  def wrap(actorRef: ActorRef) = new Wrapper(actorRef)
  def create(actorSystem: ActorSystem, name: String): Wrapper =
    wrap(actorSystem.actorOf(Props[KeyBroadCastActor], name))

  class Wrapper(val actorRef: ActorRef){
    def addOutput(key: String, out: WebSocketProcessor[String, String]): Unit =
      actorRef ! AddOutput(key, out)
    def removeOutput(key: String, out: WebSocketProcessor[String, String]): Unit =
      actorRef ! RemoveOutput(key, out)
    def boardCast(key: String, onEach: WebSocketProcessor[String, String] => Unit):Unit =
      actorRef ! BoardCast(key, onEach)
    def getSize(key: String): Future[Int] =
      (actorRef ? GetSize(key)).mapTo[Int]
  }
}