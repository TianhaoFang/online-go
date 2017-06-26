package models

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

class FriendDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                         (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import util.MyPostgresDriver.api._

  class FriendTable(tag: Tag) extends Table[(String, String, String)](tag, "Friend"){
    def user1 = column[String]("user1", O.PrimaryKey)
    def user2 = column[String]("user2", O.PrimaryKey)
    def url = column[String]("url")

    def * = (user1, user2, url)
  }
}
