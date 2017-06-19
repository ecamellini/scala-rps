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

  case class Error(message: String)

  @path("rps")
  trait RpsController {

    @command
    def play(
      move: String
    ): Future[Either[Error, PlayResult]]
  }

  class RpsControllerImpl() extends RpsController {
    def play(
      move: String
    ): Future[Either[Error, PlayResult]] = {
      RPS.run(move) match {
        case Some((result, userMove, computerMove)) => {
          val res = PlayResult(result, userMove, computerMove)
          val future = ResultDataModule.createResult(res)
          future.map(_ => Right(res)) // The DB interaction directly returs future
        }
        case None => Future(Left(Error(s"${move} is an invalid move")))
      }
    }
  }
}

object errors {
  import FailSupport._
  import controllers.Error

  import io.circe.syntax._
  implicit def errorToResponse = new ToHttpResponse[Error] {
    def response(error: Error) = HttpResponse(
      status = StatusCodes.UnprocessableEntity,
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
