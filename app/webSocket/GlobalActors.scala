package webSocket

import javax.inject.Inject
import javax.inject.Singleton

import akka.actor.{ActorRef, ActorSystem, Props}

import scala.concurrent.ExecutionContext

@Singleton
class GlobalActors @Inject()(val system: ActorSystem, private implicit val context: ExecutionContext) {
  val messageBoarder: KeyBroadCastActor.Wrapper = KeyBroadCastActor.create(system, "messageBoarder")
  val gameBoarder: KeyBroadCastActor.Wrapper = KeyBroadCastActor.create(system, "gameBoarder")
  val userStatusBoarder: KeyBroadCastActor.Wrapper = KeyBroadCastActor.create(system, "userStatusBoarder")
  val waitListActor: WaitListActor.Wrapper = WaitListActor.create(system, "waitListActor")

  GlobalActors.instance = this
}

object GlobalActors {
  var instance: GlobalActors = _
}