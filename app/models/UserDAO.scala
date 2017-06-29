package models

import javax.inject.Inject
import javax.inject.Singleton

import com.fang.UserModel
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.Effect
import slick.driver.JdbcProfile
import slick.profile.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
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

  val users: TableQuery[UserTable] = TableQuery[UserTable]

  def getUserByName(username: String): Future[Option[UserModel]] = db.run(
    users.filter(_.username === username).result.headOption
  )

  def insertUser(userModel: UserModel): Future[Int] = {
    db.run(users += userModel)
  }
}