package rps

import models._

import slick._
import slick.jdbc.PostgresProfile.api._
import scala.reflect.ClassTag
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import io.buildo.enumero.{ CaseEnum, CaseEnumSerialization }

// Let's tell Slick how to translate the RPS.move enum into the SQL DB
// this allows us to use column[RPS.Move]
object EnumeroSupport {
  implicit def enumeroColumnType[A <: CaseEnum : ClassTag](implicit ces: CaseEnumSerialization[A]) = 
  MappedColumnType.base[A, String](
    { move => ces.caseToString(move) },
    { moveStr => ces.caseFromString(moveStr).get }
  )
}

object Db {
  import EnumeroSupport._

  class GameResult(tag: Tag) extends Table[PlayResult](tag, "GAME_RESULT") {
    // This looks for the MappedColumnType that we defined above
    // It actually looks for a MappedColumnType for RPS.Move, then it does not find it and
    // looks for the same for a CaseEnum, since RPS.Move is a CaseEnum
    def userMove = column[RPS.Move]("USER_MOVE")
    def computerMove = column[RPS.Move]("COMPUTER_MOVE")
    def result = column[Result]("RESULT")

    def * = (result, userMove, computerMove) <> (PlayResult.tupled, PlayResult.unapply)
  }
  val gameResults = TableQuery[GameResult]

  val db = Database.forConfig("postgresdb")
}


object ResultDataModule {
  import Db._

  def createResult(p: PlayResult): Future[Int] =
    db.run(gameResults += p)

  def getResults(): Future[List[PlayResult]] =
    db.run(gameResults.result).map(_.toList)
}
