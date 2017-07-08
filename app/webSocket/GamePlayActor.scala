package webSocket

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import com.fang.game.{GameStatus, Status, Step}
import webSocket.GamePlayActor._

import scala.collection.mutable.{HashMap => HMap}
import scala.concurrent.Future

class GamePlayActor extends Actor{
  var map: HMap[UUID, GameValue] = new HMap()

  override def receive: Receive = {
    case command: Command => command match {
      case CreateGame(uuid, rule) =>
        val result: Boolean = map.get(uuid) match {
          case Some(_) => false
          case None =>
            map.put(uuid, GameValue(GameStatus(rule), System.currentTimeMillis()))
            true
        }
        sender() ! result
      case PutStep(uuid, step, turn) =>
        val result: StepResult = map.get(uuid) match {
          case Some(gameValue) =>
            gameValue.lastTime = System.currentTimeMillis()
            gameValue.gameStatus.put(step, turn) match {
              case Left(message) =>
                  gameValue.gameStatus.getError match {
                  case Some(error) => HasError(error)
                  case None => Invalid(message)
                }
              case Right(status) => Normal(status, gameValue.gameStatus.step)
            }
          case None => NotFound(uuid)
        }
        sender() ! result
    }
    case r => throw new Exception("unknown type " + r)
  }
}

object GamePlayActor {
  sealed trait Command
  case class CreateGame(uuid: UUID, rule: String) extends Command
  case class PutStep(uuid: UUID, step: Step, turn: Int) extends Command

  sealed trait StepResult
  case class NotFound(uuid: UUID) extends StepResult
  case class Invalid(message: String) extends StepResult
  case class HasError(exception: Exception) extends StepResult
  case class Normal(status: Status, step: Int) extends StepResult

  case class GameValue(var gameStatus: GameStatus, var lastTime: Long)

  class Wrapper(val actorRef: ActorRef){
    def createGame(uuid: UUID, rule: String): Future[Boolean] =
      (actorRef ? CreateGame(uuid, rule)).mapTo[Boolean]
    def putStep(uuid: UUID, step: Step, turn: Int): Future[StepResult] =
      (actorRef ? PutStep(uuid, step, turn)).mapTo[StepResult]
  }

  def wrap(actorRef: ActorRef): Wrapper = new Wrapper(actorRef)
  def create(actorSystem: ActorSystem, name: String): Wrapper =
    wrap(actorSystem.actorOf(Props[GamePlayActor], name))
}