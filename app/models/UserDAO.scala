package models

import javax.inject.Inject
import javax.inject.Singleton

import com.fang.{LoginRequest, UserModel}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.dbio.{DBIOAction, Effect}
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

  class AdminTable(tag: Tag) extends Table[LoginRequest](tag, "admin") {
    def username = column[String]("username", O.PrimaryKey)
    def password = column[String]("password")

    def * = (username, password) <> ((LoginRequest.apply _).tupled, LoginRequest.unapply)
  }

  val admins: TableQuery[AdminTable] = TableQuery[AdminTable]

  def getUserByName(username: String): Future[Option[UserModel]] = db.run(
    users.filter(_.username === username).result.headOption
  )

  def insertUser(userModel: UserModel): Future[Int] = {
    db.run(users += userModel)
  }

  def updateUser(username: String, userModel: UserModel.NoPassword): Future[Int] = {
    db.run(
      users.filter(_.username === username).result.headOption.flatMap{
        case None => DBIOAction.successful(0)
        case Some(prev) =>
          val u = userModel.copy(
            google_id = if(userModel.google_id.isDefined) userModel.google_id else prev.google_id,
            image_url = if(userModel.image_url.isDefined) userModel.image_url else prev.image_url
          )
          users.filter(_.username === username).map(s => (s.nickname, s.email, s.google_id, s.image_url))
            .update(u.nickname, u.email, u.google_id, u.image_url)
      }.transactionally)
  }

  def updatePassword(username: String, encodedPassword: String): Future[Int] = db.run(
    users.filter(_.username === username).map(_.password)
      .update(encodedPassword)
  )

  def getAdmin(username: String): Future[Option[LoginRequest]] = db.run(
    admins.filter(_.username === username).result.headOption
  )

  def updateAdmin(loginRequest: LoginRequest): Future[Int] = db.run(
    admins.filter(_.username === loginRequest.username)
      .map(_.password).update(loginRequest.password)
  )

  def searchUser(userName: String): Future[Seq[String]] = db.run(
    users.filter(_.username.startsWith(userName)).map(_.username).result
  )
}