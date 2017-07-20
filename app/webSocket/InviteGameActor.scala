package webSocket

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import com.fang.UserStatus.InviteStatus
import webSocket.InviteGameActor._

import scala.collection.mutable.{HashMap => MutMap}
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, HOURS}

class InviteGameActor extends Actor {
  val userMap: MutMap[String, InviteStatus] = MutMap()

  override def receive: Receive = {
    case ivCommand: IVCommand => ivCommand match {
      case MakeInvite(user1, user2, rule) =>
        val message: Either[String, Boolean] =
          if (userMap.get(user1).isDefined) {
            Left("user:" + user1 + " is already inviting others")
          } else if (userMap.get(user2).isDefined) {
            Left("user:" + user2 + " is already invited by others")
          } else {
            val status = InviteStatus(user1, user2, rule)
            userMap.put(user1, status)
            userMap.put(user2, status)
            Right(true)
          }
        sender() ! message
      case RemovePendingInvite(user1, user2) =>
        val invite1 = userMap.get(user1)
        val invite2 = userMap.get(user2)
        val message: Either[String, InviteStatus] =
          if (invite1.isEmpty || invite2.isEmpty) {
            Left(s"no such invitation $user1->$user2")
          } else if (invite1.get != invite2.get) {
            Left(s"$user1 and $user2 are not in the same invitation")
          } else if (invite1.get.user1 != user1 || invite1.get.user2 != user2) {
            Left(s"$user1 and $user2 are not in the same invitation")
          } else {
            userMap.remove(user1)
            Right(userMap.remove(user2).get)
          }
        sender() ! message
      case QueryInvite(user) =>
        val result: Option[InviteStatus] = userMap.get(user)
        sender() ! result
    }
    case other: Any => throw new Exception("not find type" + other.getClass.getName)
  }
}

object InviteGameActor {

  implicit val timeOut = akka.util.Timeout(Duration(2, HOURS))

  sealed trait IVCommand

  case class MakeInvite(user1: String, user2: String, rule: String) extends IVCommand

  case class RemovePendingInvite(user1: String, user2: String) extends IVCommand

  case class QueryInvite(user: String) extends IVCommand

  class Wrapper(actorRef: ActorRef) {
    def makeInvite(user1: String, user2: String, rule: String): Future[Either[String, Boolean]] = {
      (actorRef ? MakeInvite(user1, user2, rule)).mapTo[Either[String, Boolean]]
    }

    def removePendingInvite(user1: String, user2: String): Future[Either[String, InviteStatus]] = {
      (actorRef ? RemovePendingInvite(user1, user2)).mapTo[Either[String, InviteStatus]]
    }

    def queryInvite(user: String): Future[Option[InviteStatus]] = {
      (actorRef ? QueryInvite(user)).mapTo[Option[InviteStatus]]
    }
  }

  def wrap(actorRef: ActorRef): Wrapper = new Wrapper(actorRef)
  def create(actorSystem: ActorSystem, name: String): Wrapper =
    wrap(actorSystem.actorOf(Props[InviteGameActor], name))
}
