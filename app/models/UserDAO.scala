package models

import javax.inject.Inject

import com.fang.UserModel
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.lifted.Tag

import scala.concurrent.ExecutionContext

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import util.MyPostgresDriver.api._

  class UserTable(tag: Tag) extends Table[UserModel](tag, "User") {
    def username = column[String]("username", O.PrimaryKey)
    def password = column[String]("password")
    def nickname = column[String]("nickname")
    def email = column[String]("email", O.Default(""))
    def google_id = column[Option[String]]("google_id")
    def image_url = column[Option[String]]("image_url")

    def * = (username, password, nickname, email, google_id, image_url) <>
      ((UserModel.apply _).tupled, UserModel.unapply)
  }

}