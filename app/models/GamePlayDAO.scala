package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import com.fang.GamePlayJson
import com.fang.game.Step
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class GamePlayDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import util.MyPostgresDriver.api._

  class GamePlayTable(tag: Tag) extends Table[GamePlayModel](tag, "gameplay") {
    def id = column[UUID]("id", O.PrimaryKey)

    def first_user = column[String]("first_user")

    def second_user = column[String]("second_user")

    def status = column[String]("status")

    def rule = column[String]("rule")

    def first_win = column[Option[Boolean]]("first_win")

    def start_time = column[Timestamp]("start_time")

    def steps = column[List[Step]]("steps")

    def * = (id, first_user, second_user, status, rule, first_win, start_time, steps) <>
      ((GamePlayModel.apply _).tupled, GamePlayModel.unapply)
  }

  val gamePlays: TableQuery[GamePlayTable] = TableQuery[GamePlayTable]

  def containUser(id: String): (GamePlayTable) => Rep[Boolean] =
    (g: GamePlayTable) => g.first_user === id || g.second_user === id

  def queryRunningGame(userId: String): Future[Option[GamePlayModel]] = db.run(
    gamePlays.filter(g => containUser(userId)(g) && g.status === GamePlayJson.PLAYING).result.headOption
  )

  def queryPlayingGame(): Future[Seq[GamePlayModel]] = db.run(
    gamePlays.sortBy(_.start_time.desc).take(10).result
  )

  def intSeq2Str(seq: List[Int]): String = {
    val builder = new StringBuilder
    var isFirst = true
    builder.append("{")
    for (item <- seq) {
      if (isFirst) {
        isFirst = false
      } else {
        builder.append(", ")
      }
      builder.append(item)
    }
    builder.append("}")
    builder.toString()
  }

  def createGame(gamePlayModel: GamePlayModel): Future[Boolean] = {
    val m = gamePlayModel
    val stepStr = intSeq2Str(m.steps.map(_.toInt))
    val stat = sqlu"""INSERT INTO gameplay(id, first_user, second_user, status, rule, first_win, start_time, steps)
           VALUES (${m.id.toString}::uuid, ${m.first_user}, ${m.second_user}, ${m.status}, ${m.rule}, ${m.first_win},
      ${m.start_time}, ${stepStr}::integer[])"""
    db.run(stat).map(_ > 0).recover{
      case exception: Exception =>
        exception.printStackTrace()
        false
    }
  }

  def queryGame(id: String): Future[Option[GamePlayModel]] = {
    val uid = Try(UUID.fromString(id))
    if (uid.isFailure) Future.successful(None)
    else db.run(gamePlays.filter(_.id === uid.get).result.headOption)
  }

  def updateGame(id: String, gamePlayModel: GamePlayModel): Future[Int] = {
    val uid = Try(UUID.fromString(id))
    if (uid.isFailure) return Future.successful(0)
    val m = gamePlayModel
    val stepStr = intSeq2Str(m.steps.map(_.toInt))
    val statement =
      sqlu"""UPDATE gameplay SET status = ${m.status}, first_win = ${m.first_win},
            steps = ${stepStr}::integer[] WHERE id = ${id}::uuid
          """
    db.run(statement)
  }
}
