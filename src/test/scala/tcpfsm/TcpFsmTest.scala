package tcpfsm

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestFSMRef, TestKit }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import tcpfsm.TcpFsm.{ Ack, Closed, Established, Listen, ListenForConnection, Syn, SynReceived }

class TcpFsmTest extends TestKit(ActorSystem("test-system")) with AnyWordSpecLike with Matchers with ImplicitSender {
  "TcpFsm" should {
    "go through the expected state transitions" in {
      val server = TestFSMRef(new TcpFsm, "server")
      server.stateName shouldBe Closed

      server ! ListenForConnection
      server.stateName shouldBe Listen
      expectNoMessage()

      server ! Syn
      server.stateName shouldBe SynReceived
      expectMsg(Syn)
      expectMsg(Ack)

      server ! Ack
      server.stateName shouldBe Established

      // server.isTimerActive("...")
    }
  }
}
