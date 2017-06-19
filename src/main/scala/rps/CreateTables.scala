package rps

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object CreateTables extends App {
  // Create the tables, including primary and foreign keys
  val setupFuture = Db.db.run(Db.gameResults.schema.create)
  Await.result(setupFuture, 1 second)
}
