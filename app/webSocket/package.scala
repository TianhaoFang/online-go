import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.iteratee.Enumerator

package object webSocket {
  sealed abstract class KBCommand
  case class AddOutput(key: String, out: Channel[String]) extends KBCommand
  case class RemoveOutput(key: String, out: Channel[String]) extends KBCommand
  case class BoardCast(key: String, onEach: Channel[String] => Unit) extends KBCommand
}
