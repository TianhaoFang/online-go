import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator

package object webSocket {
  sealed abstract class KBCommand
  case class AddOutput(key: String, out: WebSocketProcessor[String, String]) extends KBCommand
  case class RemoveOutput(key: String, out: WebSocketProcessor[String, String]) extends KBCommand
  case class BoardCast(key: String, onEach: WebSocketProcessor[String, String] => Unit) extends KBCommand
  case class GetSize(key: String) extends KBCommand

  sealed trait WLCommand
  case class AddToWaitList(rule: String, userId: String) extends WLCommand
  case class RemoveFromWaitList(userId: String) extends WLCommand
  case class CountWaitList() extends WLCommand
  case class GetUserStatus(userId: String) extends WLCommand
}
