package rps // This is to access all the stuff in RPS
package test

import org.scalatest.{ FlatSpec, FunSpec, Matchers}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._

import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.buildo.enumero.circe._

import org.scalacheck.{ Properties, Gen }
import org.scalacheck.Prop.{ forAll, BooleanOperators }
import org.scalatest.prop.Checkers

import io.buildo.enumero.CaseEnumSerialization

class RPSSpec extends FlatSpec with ScalatestRouteTest with Matchers {

  val router = WebServer.route

  " when move is Rock" should "work correctly" in {
    Post("/rps/play", WebServer.PlayPayload("Rock")) ~> router ~> check {
      status shouldBe OK // Here we are using the shouldBe matcher
      responseAs[WebServer.PlayResult].userMove shouldBe RPS.RPSMove.Rock
    }
  }

  "when sending an invalid move" should "get a 422 status" in {
    Post("/rps/play", Map("userMove" -> "Invalid")) ~> router ~> check {
      status shouldBe UnprocessableEntity
    }
  }

  val wiroRouter = WiroWebServer.rpsController.buildRoute
  
  "when move is Rock" should "work correctly also with wiro" in {
    Post("/rps/play", Map("move" -> "Rock")) ~> wiroRouter ~> check {
      status shouldBe OK // Here we are using the shouldBe matcher
      responseAs[WebServer.PlayResult].userMove shouldBe RPS.RPSMove.Rock
    }
  }

}

class RPSProperties extends FunSpec with Checkers {

  import RPS._

  val PlayResultGen: Gen[WebServer.PlayResult] = for {
    userMove <- Gen.oneOf(RPSMove.values.map(RPSMove.caseToString).toList)
    result = WebServer.PlayResult.tupled(RPS.run(userMove).get) //  returns an option, so we use get (strings are hardcoded, will never be None)
    if result.result == Result.Win
  } yield result

  describe("RPS Game") {
    it("should result in a win only when user move beats computer move") {
      val prop = forAll(PlayResultGen) {
          case WebServer.PlayResult(_, RPS.RPSMove.Rock, RPS.RPSMove.Scissors) => true
          case WebServer.PlayResult(_, RPS.RPSMove.Paper, RPS.RPSMove.Rock) => true
          case WebServer.PlayResult(_, RPS.RPSMove.Scissors, RPS.RPSMove.Paper) => true
          case _ => false
        }
      check(prop)
    }
  }
}
