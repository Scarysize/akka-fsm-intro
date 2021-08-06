# Testen

Akka stellt den `TestFSMRef` Helfer zur Verfügung. Damit kann im Test auf den internen Zustand und die Daten zugegriffen werden.

```scala
val fsm = TestFSMRef(new TrafficLight)

fsm.stateName shouldBe Green

fsm ! StateTimeout
fsm.stateName shouldBe Yellow
fsm.stateData shouldBe NotUsed
```
