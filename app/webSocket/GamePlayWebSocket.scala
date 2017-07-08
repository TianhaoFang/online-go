package webSocket

import java.util.UUID

import com.fang.game.Step

import scala.concurrent.ExecutionContext

class GamePlayWebSocket
(
  val gameId: UUID,
  val globalActors: GlobalActors
)(
  private implicit val executionContext: ExecutionContext
) extends WebSocketProcessor[String, String] {
  override def onReceive(message: String): Unit = {}

  override def onConnected(): Unit = {
    globalActors.gameBoarder.addOutput(gameId.toString, this)
  }

  override def onCloseClient(): Unit = {
    globalActors.gameBoarder.removeOutput(gameId.toString, this)
  }
}
