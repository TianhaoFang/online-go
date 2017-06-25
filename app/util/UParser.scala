package util

import akka.stream.scaladsl.Sink
import akka.util.ByteString
import play.api.libs.streams.Accumulator
import play.api.mvc
import play.api.mvc.{BodyParser, RequestHeader, Results}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object UParser {
  @inline
  def apply[A](mapper: String => A): BodyParser[A] = new BodyParser[A] {
    override def apply(req: RequestHeader): Accumulator[ByteString, Either[mvc.Result, A]] = {
      val sink: Sink[ByteString, Future[ByteString]] = Sink.fold(ByteString.empty)(_ ++ _)
      Accumulator(sink)
        .map(_.utf8String)
        .map(str => Try(mapper(str)))
        .map{
          case Success(a) => Right(a)
          case Failure(error) => Left(Results.BadRequest(error.getMessage))
        }
    }
  }
}
