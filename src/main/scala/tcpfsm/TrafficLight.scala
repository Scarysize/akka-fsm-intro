package tcpfsm

import akka.NotUsed
import akka.actor.FSM
import tcpfsm.TrafficLight._

import scala.concurrent.duration.DurationInt

object TrafficLight {
  sealed trait State
  case object Green extends State
  case object Yellow extends State
  case object Red extends State
}

class TrafficLight extends FSM[State, NotUsed] {
  startWith(Green, NotUsed, Option(10.seconds))

  when(Green) {
    case Event(StateTimeout, _) =>
      goto(Yellow).forMax(5.seconds)
  }

  when(Yellow) {
    case Event(StateTimeout, _) =>
      goto(Red).forMax(10.seconds)
  }

  when(Red) {
    case Event(StateTimeout, _) =>
      goto(Green).forMax(10.seconds)
  }

  onTransition {
    case (from, to) =>
      println(s"Change light from $from to $to")
  }
}
