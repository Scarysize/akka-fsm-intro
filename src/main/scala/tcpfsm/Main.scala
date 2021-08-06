package tcpfsm

import akka.actor.FSM.{ CurrentState, SubscribeTransitionCallBack, Transition }
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import tcpfsm.TcpFsm.{ Connect, Disconnect, ListenForConnection, WriteToConnection }

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.DurationInt

object Main {
  def main(args: Array[String]): Unit = {
    implicit val timeout: Timeout = Timeout(1.second)
    implicit val system: ActorSystem = ActorSystem()

    val server = system.actorOf(Props(new TcpFsm), "server")
    val _ = system.actorOf(Props(new FsmMonitor(server)))

    server ! ListenForConnection

    val client = system.actorOf(Props(new TcpFsm), "client")
    val _ = system.actorOf(Props(new FsmMonitor(client)))

    client ! Connect(server, () => {
      for {
        _ <- client ? WriteToConnection("hello from the client")
        _ <- server ? WriteToConnection("hello from the server")
      } yield {
        client ! Disconnect
      }
    })
  }

  private class FsmMonitor(target: ActorRef) extends Actor {
    override def preStart(): Unit = {
      target ! SubscribeTransitionCallBack(self)
    }

    override def receive: Receive = {
      case CurrentState(_, state) =>
        println(s"${target.path.name.toUpperCase()}: Current state is $state")
      case Transition(_, from, to) =>
        println(s"${target.path.name.toUpperCase()}: Transitioning state from '$from' to '$to'")
    }
  }
}
