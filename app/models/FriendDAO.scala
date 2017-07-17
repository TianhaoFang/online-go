package models

import javax.inject.Inject

import com.fang.FriendModel
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class FriendDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                         (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import util.MyPostgresDriver.api._

  class FriendTable(tag: Tag) extends Table[FriendModel](tag, "friend"){
    def user1 = column[String]("user1", O.PrimaryKey)
    def user2 = column[String]("user2", O.PrimaryKey)
    def url = column[String]("url")
    def accepted = column[Boolean]("accepted")

    def * = (user1, user2, url, accepted) <> ((FriendModel.apply _).tupled, FriendModel.unapply)
  }

  val friends: TableQuery[FriendTable] = TableQuery[FriendTable]

  def findFriend(user1: String, user2: String): Future[Option[FriendModel]] = db.run(
    friends.filter(f => (f.user1 === user1 && f.user2 === user2 && f.accepted) ||
      (f.user2 === user1 && f.user1 === user2 && f.accepted))
      .result.headOption
  )

  def findFriend(user: String): Future[Seq[FriendModel]] = db.run(
    friends.filter(f => (f.user1 === user || f.user2 === user) && f.accepted).result
  ).map(_.map(f => {
    if(f.user1 == user) f
    else f.copy(user1 = f.user2, user2 = f.user1)
  }))

  def findInvented(user: String): Future[Seq[FriendModel]] = db.run(
    friends.filter(f => (f.user1 === user || f.user2 === user) && f.accepted === false).result
  )

  def findInvented(user1: String, user2: String): Future[Option[FriendModel]] = db.run(
    friends.filter(f => (f.user1 === user1 && f.user2 === user2 && !f.accepted) ||
      (f.user2 === user1 && f.user1 === user2 && !f.accepted))
      .result.headOption
  )

  def deleteRelation(user1: String, user2: String): Future[Int] = db.run(
    friends.filter(f => (f.user1 === user1 && f.user2 === user2) ||
      (f.user2 === user1 && f.user1 === user2)).delete
  )

  def updateRelation(friendModel: FriendModel): Future[Int] = db.run(
    friends.filter(f => f.user1 === friendModel.user1 && f.user2 === friendModel.user2)
      .update(friendModel)
  )

  def createRelation(user1: String, user2: String): Future[Int] = db.run(
    friends.filter(f => (f.user1 === user1 && f.user2 === user2) ||
      (f.user2 === user1 && f.user1 === user2)).result.headOption
  ).flatMap {
    case Some(_) => Future.successful(0)
    case None =>
      db.run(friends += FriendModel(user1, user2, "", accepted = false))
  }
}
