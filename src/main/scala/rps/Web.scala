package rps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.buildo.enumero.circe._
import akka.http.scaladsl.model.StatusCodes._ // This allows us to call complete() with two args

object WebServer extends App {
  import models._

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  case class PlayPayload(userMove: String)
  case class Error(message: String)

  // Get the implicit instance of this thing and return it.
  // implicitly is useful to check if there is the implicit we need in the scope.
  //implicitly[io.circe.Encoder[PlayResult]]

  // def is to solve an exception in tests. To avoid it and use val put this in an object
  def route =
    pathPrefix("rps"){
      path("play") {
        post {
          entity(as[PlayPayload]) { payload =>
            RPS.run(payload.userMove) match {
              case Some((result, userMove, computerMove)) => complete(PlayResult(result, userMove, computerMove))
              case None => complete(UnprocessableEntity, Error(s"${payload.userMove} is an invalid move"))
            }
          }
        }
      } 
    } ~
    options(complete("ok")) // to say to the browser that we support everything

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
