package util

import com.fang.game.Step
import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.array.PgArrayExtensions
import slick.driver.JdbcProfile
import slick.profile.Capability

trait MyPostgresDriver
  extends ExPostgresDriver
    with PgArraySupport with PgDateSupport
    with PgRangeSupport with PgHStoreSupport{
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcProfile.capabilities.insertOrUpdate

  override val api = MyApi

  object MyApi
    extends API with ArrayImplicits with DateTimeImplicits
      with RangeImplicits with HStoreImplicits{
    implicit val intListTypeMapper: DriverJdbcType[List[Int]] =
      new SimpleArrayJdbcType[Int]("integer").to(_.toList)
    implicit val stepListTypeMapper: DriverJdbcType[List[Step]] =
      new SimpleArrayJdbcType[Int]("integer").mapTo[Step](Step.fromInt, _.toInt).to(_.toList)
  }
}

object MyPostgresDriver extends MyPostgresDriver {}