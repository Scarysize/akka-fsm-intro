package tcpfsm

import akka.actor.{ ActorRef, FSM }
import tcpfsm.TcpFsm._

import scala.concurrent.duration.DurationInt

object TcpFsm {
  // State names, these represents the nodes of the state machine
  sealed trait ConnectionState
  case object Closed extends ConnectionState
  case object Listen extends ConnectionState
  case object SynSent extends ConnectionState
  case object SynReceived extends ConnectionState
  case object Established extends ConnectionState
  case object CloseWait extends ConnectionState
  case object LastAck extends ConnectionState
  case object Closing extends ConnectionState
  case object FinWait1 extends ConnectionState
  case object FinWait2 extends ConnectionState
  case object TimeWait extends ConnectionState

  // State data, these capture the actual state data
  sealed trait StateData
  case object Empty extends StateData
  case class UninitializedConnection(target: ActorRef, onEstablished: Option[() => Unit] = None) extends StateData
  case class OpenConnection(target: ActorRef) extends StateData

  // TCP messages
  sealed trait TCPPacket
  case object Syn extends TCPPacket
  case object Ack extends TCPPacket
  case object Fin extends TCPPacket
  case class Data(payload: String) extends TCPPacket

  // "Commands", the public interface
  sealed trait Command
  case class Connect(target: ActorRef, onConnect: () => Unit) extends Command
  case object ListenForConnection extends Command
  case object Disconnect extends Command
  case class WriteToConnection(data: String) extends Command

  // Internal control messages
  private case object AppClose
  private case object Timeout
}

class TcpFsm extends FSM[ConnectionState, StateData] {

  // The initial state
  startWith(Closed, Empty)

  when(Closed) {
    case Event(Connect(target, onConnect), Empty) =>
      goto(SynSent).using(UninitializedConnection(target, Option(onConnect)))
    case Event(ListenForConnection, Empty) =>
      goto(Listen)
  }

  onTransition {
    case Closed -> SynSent =>
      nextStateData match {
        case conn: UninitializedConnection =>
          // When transitioning from Closed to SynSent, actually do send a SYN to the target.
          conn.target ! Syn
        case _ =>
      }
  }

  when(Listen) {
    case Event(Syn, Empty) =>
      goto(SynReceived).using(UninitializedConnection(sender()))
  }

  when(SynSent) {
    case Event(Syn, _: UninitializedConnection) =>
      goto(SynReceived)
  }

  onTransition {
    case SynSent -> SynReceived =>
      nextStateData match {
        case conn: UninitializedConnection =>
          // Acknowledge the received SYN with an ACK
          conn.target ! Ack
        case _ =>
      }
    case Listen -> SynReceived =>
      nextStateData match {
        case conn: UninitializedConnection =>
          // Do a SYN+ACK, to acknowledge the received SYN while emitting your own SYN. Order is important!
          conn.target ! Syn
          conn.target ! Ack
        case _ =>
      }
  }

  when(SynReceived) {
    case Event(Ack, conn: UninitializedConnection) =>
      goto(Established).using(OpenConnection(conn.target))
  }

  onTransition {
    case _ -> Established =>
      stateData match {
        case UninitializedConnection(_, Some(callback)) =>
          callback.apply()
        case _ =>
      }
  }

  when(Established) {
    case Event(Data(payload), conn: OpenConnection) =>
      println(s"${self.path.name} Received data: \"$payload\"")
      conn.target ! Ack
      stay()
    case Event(Ack, _: OpenConnection) =>
      stay()
    case Event(Fin, _: OpenConnection) =>
      goto(CloseWait)

    // Public API once the connection is up
    case Event(WriteToConnection(payload), conn: OpenConnection) =>
      conn.target ! Data(payload)
      sender() ! ()
      stay()
    case Event(Disconnect, _: OpenConnection) =>
      goto(FinWait1)
  }

  onTransition {
    case Established -> CloseWait =>
      nextStateData match {
        case conn: OpenConnection =>
          conn.target ! Ack
          startSingleTimer("app-close-signal", AppClose, 3.seconds)
        case _ =>
      }
    case Established -> FinWait1 =>
      nextStateData match {
        case conn: OpenConnection =>
          conn.target ! Fin
        case _ =>
      }
  }

  when(CloseWait) {
    case Event(AppClose, _: OpenConnection) =>
      goto(LastAck)
  }

  when(FinWait1) {
    case Event(Ack, _: OpenConnection) =>
      goto(FinWait2)
    case Event(Fin, _: OpenConnection) =>
      goto(Closing)
  }

  onTransition {
    case CloseWait -> LastAck =>
      nextStateData match {
        case conn: OpenConnection =>
          conn.target ! Fin
        case _ =>
      }
    case FinWait1 -> Closing =>
      nextStateData match {
        case conn: OpenConnection =>
          conn.target ! Ack
        case _ =>
      }
  }

  when(LastAck) {
    case Event(Ack, _: OpenConnection) =>
      goto(Closed).using(Empty)
  }

  when(FinWait2) {
    case Event(Fin, _: OpenConnection) =>
      goto(TimeWait)
  }

  when(Closing) {
    case Event(Ack, _: OpenConnection) =>
      goto(TimeWait)
  }

  onTransition {
    case FinWait2 -> TimeWait =>
      nextStateData match {
        case conn: OpenConnection =>
          conn.target ! Ack
          startSingleTimer("timeout", Timeout, 3.seconds)
        case _ =>
      }
    case Closing -> TimeWait =>
      startSingleTimer("timeout", Timeout, 3.seconds)
  }

  when(TimeWait) {
    case Event(Timeout, _: OpenConnection) =>
      goto(Closed).using(Empty)
  }
}
