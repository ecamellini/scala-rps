package rps

import scala.concurrent.Future
import wiro.server.akkaHttp.{ RouterDerivationModule, ToHttpResponse, FailSupport, HttpRPCServer }
import wiro.models.Config

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import akka.http.scaladsl.model.MediaTypes

import io.circe.generic.auto._
//import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.buildo.enumero.circe._

object models {
  case class PlayResult(result: Result, userMove: RPS.Move, computerMove: RPS.Move)
}

object controllers {
  import models._
  import wiro.annotation._
  import FailSupport._

  case class UnprocessableEntityError(message: String)
  case class InternalServerError(message: String)

  @path("rps")
  trait RpsController {

    @command
    def play(
      move: String
    ): Future[Either[UnprocessableEntityError, PlayResult]]

    @query
    def results(): Future[Either[InternalServerError, List[PlayResult]]]
  }

  class RpsControllerImpl() extends RpsController {
    def play(
      move: String
    ): Future[Either[UnprocessableEntityError, PlayResult]] = {
      RPS.run(move) match {
        case Some((result, userMove, computerMove)) => {
          val res = PlayResult(result, userMove, computerMove)
          val future = ResultDataModule.createResult(res)
          future.map(_ => Right(res)) // The DB interaction directly returs future
        }
        case None => Future(Left(UnprocessableEntityError(s"${move} is an invalid move")))
      }
    }

    def results(): Future[Either[InternalServerError, List[PlayResult]]] = {
      ResultDataModule.getResults().map(Right(_))
    }
  }
}

object errors {
  import FailSupport._
  import controllers.UnprocessableEntityError
  import controllers.InternalServerError

  import io.circe.syntax._
  implicit def unprocessableEntityErrorToResponse = new ToHttpResponse[UnprocessableEntityError] {
    def response(error: UnprocessableEntityError) = HttpResponse(
      status = StatusCodes.UnprocessableEntity,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }

  implicit def internalServerErrorToResponse = new ToHttpResponse[InternalServerError] {
    def response(error: InternalServerError) = HttpResponse(
      status = StatusCodes.InternalServerError,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }
}

object WiroWebServer extends App with RouterDerivationModule {
  import controllers._
  import wiro.reflect._
  import models._
  import errors._
  import FailSupport._

  def rpsController = deriveRouter[RpsController](new RpsControllerImpl)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(rpsController)
  )
}
