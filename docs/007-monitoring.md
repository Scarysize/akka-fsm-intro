# Monitoring

Internes Monitoring lässt sich einfach über einen "catch-all" Transitionhandler abbilden:

```scala
onTransition(handler _)

def handler(from: State, to: State): Unit = {
  // ...
}
```

Externes Monitoring ist über einen separaten Aktor möglich:

```scala
class Monitor(fsmActor: ActorRef) extends Actor {

  fsmActor ! SubscribeTransitionCallBack(self)

  def receive = {
    // Wird einmalig nach Anmeldung empfangen
    case CurrentState(_, State) =>

    // Wird bei jedem Übergang empfangen
    case Transition(_, from, to) =>

  }
}
```
