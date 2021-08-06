# Timers

Zustands-Timeouts können an der verschieden Stellen gesetzt werden:

```scala
// Der initiale Zustand läuft nach 10 Sekunden aus
startWith(State, Data, Option(10.seconds))

// Beim Eintreten in den Zustand "MyState" startet ein 5 Sekunden Timeout.
when(MyState, 5.seconds) {
  //...
}

// Beim Übergang in "MyState" startet ein 10 Sekunden Timeout. Überschreibt den Timeout im `when(...)`
goto(MyState).forMax(10)

onTransition {
  case _ =>
    // Sendet "SomeMessage" nach 5 Sekunden Verzögerung
    startSingleTimer("my-timer", SomeMessage, 5.seconds)

    // Sendet "SomeMessage" alle 3 Sekunden
    startTimerWithFixedDelay("my-interval", SomeMessage, 3.seconds)
}

// misc
cancelTimer("my-timer")
isTimerActive("my-interval")
```
