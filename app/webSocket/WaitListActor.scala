package webSocket

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.fang.game.rules.Rules
import webSocket.WaitListActor.MatchingResult

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WaitListActor extends Actor {

  import scala.collection.mutable.{HashMap => MutMap, LinkedHashSet => MutSet}

  val userMap: MutMap[String, String] = MutMap()
  val waitListMap: HashMap[String, MutSet[String]] =
    HashMap(Rules.allRules.map(s => (s, MutSet[String]())): _*)

  override def receive: Receive = {
    case command: WLCommand =>
      command match {
        case AddToWaitList(rule, userId) =>
          userMap.get(userId) match {
            case Some(_) => sender ! false
            case None =>
              waitListMap.get(rule) match {
                case None => sender ! new Exception("unknown rule " + rule)
                case Some(set) =>
                  set += userId
                  userMap += userId -> rule
                  sender ! true
              }
          }
        case RemoveFromWaitList(userId) =>
          userMap.get(userId) match {
            case None => sender ! false
            case Some(rule) =>
              waitListMap(rule) -= userId
              userMap.remove(userId)
              sender ! true
          }
        case CountWaitList() =>
          val result: HashMap[String, Int] =
            waitListMap.toSeq.map { case (k, v) => (k, v.size) }
              .foldLeft(HashMap[String, Int]())((map, pair) => map + pair)
          sender ! result
        case GetUserStatus(userId) =>
          val result: Option[String] = userMap.get(userId)
          sender() ! result
        case DoMatching() =>
          var result = Seq[MatchingResult]()
          // find the pair in the game
          for((key, value) <- waitListMap){
            WaitListActor.removeTwoValues(value) match {
              case None =>
              case Some((v1, v2)) =>
                userMap -= v1 -= v2
                result = MatchingResult(key, v1, v2) +: result
            }
          }
          sender() ! result
      }
    case other => sender ! new Exception("unknown command type: " + other)
  }
}

object WaitListActor {
  implicit val timeout: Timeout = Timeout(2, HOURS)

  case class MatchingResult(rule: String, user1: String, user2: String)

  def wrap(actorRef: ActorRef)(implicit context: ExecutionContext) =
    new Wrapper(actorRef)(context)

  def create(system: ActorSystem, name: String)(implicit context: ExecutionContext): Wrapper =
    wrap(system.actorOf(Props[WaitListActor], name))

  class Wrapper(val actorRef: ActorRef)(implicit context: ExecutionContext) {
    def addToWaitList(rule: String, userId: String): Future[Boolean] = {
      (actorRef ? AddToWaitList(rule, userId)).map {
        case bool: Boolean => bool
        case exception: Exception => throw exception
      }
    }

    def removeFromWaitList(userId: String): Future[Boolean] = {
      (actorRef ? RemoveFromWaitList(userId)).map {
        case bool: Boolean => bool
        case exception: Exception => throw exception
      }
    }

    def countWaitList(): Future[HashMap[String, Int]] = {
      (actorRef ? CountWaitList()).mapTo[HashMap[String, Int]]
    }

    def getUserStatus(userId: String): Future[Option[String]] =
      (actorRef ? GetUserStatus(userId)).mapTo[Option[String]]

    def doMatching(): Future[Seq[MatchingResult]] = {
      (actorRef ? DoMatching()).mapTo[Seq[MatchingResult]]
    }
  }

  def removeTwoValues[T](set: mutable.Set[T]): Option[(T, T)] = {
    val iterator = set.iterator
    if(!iterator.hasNext) return None
    val v1 = iterator.next()
    if(!iterator.hasNext) return None
    val v2 = iterator.next()
    set -= v1 -= v2
    Some((v1, v2))
  }
}