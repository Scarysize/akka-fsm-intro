package tcpfsm

import akka.actor.ActorSystem
import akka.actor.FSM.StateTimeout
import akka.testkit.{ ImplicitSender, TestFSMRef, TestKit }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import tcpfsm.TrafficLight.{ Green, Red, Yellow }

class TrafficLightTest
    extends TestKit(ActorSystem("test-system"))
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender {
  "Traffic light" should {
    "direct traffic" in {
      val fsm = TestFSMRef(new TrafficLight)

      fsm.stateName shouldBe Green

      fsm ! StateTimeout
      fsm.stateName shouldBe Yellow

      fsm ! StateTimeout
      fsm.stateName shouldBe Red

      fsm ! StateTimeout
      fsm.stateName shouldBe Green
    }
  }
}
