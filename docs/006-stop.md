# Stoppen

Der Automat lässt sich komplett stoppen:

```scala
when(State) {
  case Event(PleaseStop, _) =>
    stop(Reason)
}

// ...

onTermination {
  case StopEvent(FSM.Normal, state, data)         =>
  case StopEvent(FSM.Shutdown, state, data)       =>
  case StopEvent(FSM.Failure(cause), state, data) =>
}
```
