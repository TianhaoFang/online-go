package controllers

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.scaladsl.Flow
import com.fang.game.Status.{BlackWin, Continue, SkipNext, WriteWin}
import com.fang.game.Step
import com.fang.{ErrorMessage, GamePlayJson, UserStatus}
import controllers.GamePlayController.makeMatchingSchedule
import models.{GamePlayDAO, GamePlayModel}
import play.api.mvc._
import webSocket.{GamePlayActor, GamePlayWebSocket, GlobalActors}
import upickle.default.{read, write}
import util.MyActions.MyAction
import util.UParser
import webSocket.GamePlayActor.{HasError, Invalid, Normal, NotFound}
import webSocket.WaitListActor.MatchingResult

import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GamePlayController @Inject()
(
  gamePlayDAO: GamePlayDAO,
  globalActors: GlobalActors,
  actorSystem: ActorSystem,
  implicit val executionContext: ExecutionContext
) extends Controller {
  // schedule to make the matching performance done every 2 seconds
  makeMatchingSchedule(actorSystem, globalActors, gamePlayDAO)

  def genError(message: String, status: Int = 400): Result =
    Status(status)(write[ErrorMessage](ErrorMessage(message)))

  def genNotFound(gameId: String): Result = genError("not find gameplay with id:" + gameId, 404)

  // the definition of the web application
  def getGamePlay(gameId: String): Action[AnyContent] = MyAction.async { implicit request =>
    gamePlayDAO.queryGame(gameId).map {
      case None => genNotFound(gameId)
      case Some(gamePlayModel) =>
        Ok(write[GamePlayJson](gamePlayModel.toStrId))
    }
  }

  def getGameStep(gameId: String, index: Int): Action[AnyContent] = MyAction.async { implicit request =>
    gamePlayDAO.queryGame(gameId).map {
      case None => genNotFound(gameId)
      case Some(gamePlayModel) =>
        val length: Int = gamePlayModel.steps.length
        if (index < 0) genError("invalid index less than 0:" + index, 404)
        else if (index >= length) {
          genError(s"the step is out of boundary 0-${length - 1}, actual $index", 404)
        } else {
          Ok(write[Step](gamePlayModel.steps(index)))
        }
    }
  }

  def putGameStep(gameId: String, index: Int): Action[Step] = MyAction.async(UParser(read[Step])) { implicit request =>
    import scala.concurrent.Future.{successful => succ}
    gamePlayDAO.queryGame(gameId).flatMap {
      case None => succ(genNotFound(gameId))
      case Some(model) =>
        val length: Int = model.steps.length
        if (!request.isValidUser(model.first_user) && !request.isValidUser(model.second_user)) {
          succ(genError(s"only logged in two players in the game could make steps", 401))
        } else if (length != index) {
          succ(genError("could only put on step for " + model.steps.length))
        } else if (!request.isValidUser(model.first_user) && length % 2 == 0) {
          succ(genError("is not your turn to make a step"))
        } else if (!request.isValidUser(model.second_user) && length % 2 == 1) {
          succ(genError("is not your turn to make a step"))
        } else if (model.status != GamePlayJson.PLAYING) {
          succ(genError("the game is not playing"))
        } else {
          innerPutStep(gameId, index, model, request.body)
        }
    }
  }

  def queryPlayingGame(): Action[AnyContent] = Action.async { implicit request =>
    gamePlayDAO.queryPlayingGame().map { seq =>
      Ok(write[Seq[GamePlayJson]](seq.map(_.toStrId)))
    }
  }

  def connectWebSocket(gameId: String): WebSocket = WebSocket.acceptOrResult { request =>
    gamePlayDAO.queryGame(gameId).map {
      case None => Left(genNotFound(gameId))
      case Some(_) =>
        Right(Flow.fromProcessor(() => new GamePlayWebSocket(UUID.fromString(gameId), globalActors)))
    }
  }

  def innerPutStep(gameId: String, index: Int, model: GamePlayModel, body: Step): Future[Result] = {
    import scala.concurrent.Future.{successful => succ}
    globalActors.gamePlayActor.putStep(UUID.fromString(gameId), body, index)
      .flatMap {
        case GamePlayActor.NotFound(uuid) =>
          succ(genNotFound(gameId))
        case Invalid(message) =>
          succ(genError(message))
        case HasError(exception) =>
          exception.printStackTrace()
          succ(genError(exception.getMessage))
        case Normal(status, _) =>
          var newModel = model.copy(steps = model.steps :+ body)
          val result = succ(Ok(write[Step](body)))
          if (status == BlackWin()) newModel = newModel.copy(first_win = Some(true), status = GamePlayJson.END)
          if (status == WriteWin()) newModel = newModel.copy(first_win = Some(false), status = GamePlayJson.END)
          gamePlayDAO.updateGame(gameId, newModel).flatMap(_ => {
            globalActors.gameBoarder.boardCast(gameId, { processor =>
              processor.sendMessage(write[(Step, Int)]((body, index)))
            })
            if (status == SkipNext()) {
              innerPutStep(gameId, index + 1, newModel, Step(0, 0)).flatMap(_ => result)
            } else {
              result
            }
          })
      }
  }
}

object GamePlayController {
  val MATCHING_INTERVAL: FiniteDuration = Duration(2, SECONDS)
  val MATCHING_DELAY: FiniteDuration = Duration(5, SECONDS)

  def makeMatchingSchedule(actorSystem: ActorSystem, globalActors: GlobalActors, gamePlayDAO: GamePlayDAO)
                          (implicit executionContext: ExecutionContext): Cancellable = {
    actorSystem.scheduler.schedule(MATCHING_DELAY, MATCHING_INTERVAL) {
      globalActors.waitListActor.doMatching().foreach { seq =>
        seq.foreach { matchingResult =>
          startNewGame(matchingResult, globalActors, gamePlayDAO)
        }
      }
    }
  }

  def startNewGame(matchingResult: MatchingResult, globalActors: GlobalActors, gamePlayDAO: GamePlayDAO)
                  (implicit executionContext: ExecutionContext): Unit = {
    val model = GamePlayModel(
      id = UUID.randomUUID(),
      first_user = matchingResult.user1,
      second_user = matchingResult.user2,
      status = GamePlayJson.PLAYING,
      rule = matchingResult.rule,
      first_win = None,
      start_time = new Timestamp(System.currentTimeMillis()),
      steps = List()
    )
    gamePlayDAO.createGame(model).foreach { bool =>
      if (bool) {
        globalActors.gamePlayActor.createGame(model.id, model.rule).foreach { _ =>
          globalActors.userStatusBoarder.boardCast(model.first_user, { processor =>
            processor.sendMessage(write[Either[ErrorMessage, UserStatus]](
              Right(UserStatus.playing(model.first_user, model.id.toString))))
          })
          globalActors.userStatusBoarder.boardCast(model.second_user, { processor =>
            processor.sendMessage(write[Either[ErrorMessage, UserStatus]](
              Right(UserStatus.playing(model.second_user, model.id.toString))
            ))
          })
        }
      } else {
        globalActors.userStatusBoarder.boardCast(model.first_user, { processor =>
          processor.sendMessage(write[Either[ErrorMessage, UserStatus]](
            Right(UserStatus.idle(model.first_user))))
        })
        globalActors.userStatusBoarder.boardCast(model.second_user, { processor =>
          processor.sendMessage(write[Either[ErrorMessage, UserStatus]](
            Right(UserStatus.idle(model.second_user))))
        })
      }
    }
  }
}