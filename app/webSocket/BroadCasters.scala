package webSocket

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}

@Singleton
class BroadCasters @Inject()(val system: ActorSystem) {
  val messageBoarder: ActorRef = system.actorOf(Props[KeyBroadCastActor], "messageBoarder")
  val cheeseBoarder: ActorRef = system.actorOf(Props[KeyBroadCastActor], "cheeseBoarder")
  BroadCasters.instance = this
}

object BroadCasters {
  var instance: BroadCasters = _
}