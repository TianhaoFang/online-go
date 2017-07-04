package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import com.fang.game.Step
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class GamePlayDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import util.MyPostgresDriver.api._

  class GamePlayTable(tag: Tag) extends Table[GamePlayModel](tag, "gameplay"){
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
    gamePlays.filter(g => containUser(userId)(g) && g.first_win.isEmpty).result.headOption
  )
}
