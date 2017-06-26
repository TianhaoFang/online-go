package models

import javax.inject.Inject

import com.fang.FriendModel
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

class FriendDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                         (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import util.MyPostgresDriver.api._

  class FriendTable(tag: Tag) extends Table[FriendModel](tag, "Friend"){
    def user1 = column[String]("user1", O.PrimaryKey)
    def user2 = column[String]("user2", O.PrimaryKey)
    def url = column[String]("url")
    def accepted = column[Boolean]("accepted")

    def * = (user1, user2, url, accepted) <> ((FriendModel.apply _).tupled, FriendModel.unapply)
  }
}
