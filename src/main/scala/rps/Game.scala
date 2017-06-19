package rps

import scala.io.StdIn.readLine
import scala.util.Random.shuffle
import io.buildo.enumero.annotations.enum
import io.buildo.enumero.{ CaseEnum, CaseEnumSerialization }

// object Main extends App {
//   RPS.play()
// }

@enum trait Result {
  Win
  Draw
  Lose
}

trait Game {
  type Move <: CaseEnum
  
  def play(): Unit = {
    val move = readLine("Your move> ")
    run(move) match {
      case None =>
        println("Invalid move. Try again...")
        play()
      case Some((result, userMove, computerMove)) =>
        import Result._
        result match {
          case Win => println("You win!")
          case Lose => println("You lose!")
          case Draw => println("It's a draw!")
        }
        println(s"User: $userMove")
        println(s"Computer: $computerMove")
    }
  }

  def run(move: String): Option[(Result, Move, Move)] = {
    val computerMove = generateMove()
    val userMove = toMove(move)

    userMove.map { userMove => getResult(userMove, computerMove) }
  }

  def getResult(userMove: Move, computerMove: Move): (Result, Move, Move)
  def toMove(move: String): Option[Move]
  def generateMove(): Move
}

object RPS extends Game {

  type Move = RPSMove

  @enum trait RPSMove {
    object Rock
    object Paper
    object Scissors
  }

  import RPSMove._

  def getResult(userMove: Move, computerMove: Move): (Result, Move, Move) = {
    import Result._
    val result = (userMove, computerMove) match {
        case (Rock, Scissors) | (Paper,Rock) | (Scissors, Paper) => Result.Win
        case (m1, m2) if m1 == m2 => Result.Draw
        case _ => Result.Lose
      }
    (result, userMove, computerMove)
  }

  def toMove(move: String): Option[Move] =
    CaseEnumSerialization[Move].caseFromString(move)

  def generateMove(): Move = {
    // We could define it without the brackets (), and then we should call it without.
    // However, () symbolizes the fact that it is not a pure function
    shuffle(CaseEnumSerialization[Move].values).head
  }
}

// MAP and stuff:
// map on an option is like doing match and handling the case None => None automatically
// It is actually a transformation of the value inside the "Box" Option, so also the
// Some() is omitted: just return the transformed thing and the Some is automatic
// userMove.map { userMove =>
//   val result = (userMove, computerMove) match {
//     case (Rock, Scissors) | (Paper,Rock) | (Scissors, Paper) => Result.Win
//     case (m1, m2) if m1 == m2 => Result.Draw
//     case _ => Result.Lose
//   }
//   (result, userMove, computerMove)
// 
// What happens if we have two  players? Two moves?
// m1.map {
//   m2.map {
//     (m1,m2) match {
//       // A
//     }
//   }
// }: Option[Option[A]]
// We have two option encapsulated. The solution would be to use flatMap instead of map
// on m1. Or to use a for comprehension:
// for {
//   userMove1 => toMove(move1)
//   userMove2 => toMove(move2)
//   computerMove = generateMove()
// } yeld {
//  ... 
// }
// flatMap or the for comprehension are such that if one of the operations fail
// everything fails. For comprehension is just flat flat flat ... map
