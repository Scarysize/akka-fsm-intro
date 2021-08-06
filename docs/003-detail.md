# `FSM` im Detail

```scala
trait FSM[S, D] extends Actor with Listeners with ActorLogging {

  // Zustand
  type State = FSM.State[S, D]

  // Ergeignisse
  type Event = FSM.Event[D]

  // ...
}
```

- **`S`** definiert den Typen der möglichen Zustände. Die Knoten im Diagram. Auch Zustandsnamen, "state designator".
- **`D`** definiert den Typen der möglichen "Daten", die der Automat intern speichert.

---

```scala
class VendingMachine extends FSM[State, Int] {
  startWith(InitialState, 0)

  when(InitialState) {
    case Event(coins: Int, sum: Int) =>
      goto(AddingCoins).using(sum + coins)
  }

  when(AddingCoins) {
    case Event(coins: Int, sum: Int) =>
      stay().using(sum + coins)
  }
}
```

- **`startWith(<State>, <Data>)`** setzt den Initialzustand.
- **`when(<State>)`** Blöcke definieren das Verhalten bei eintretenden Ereignissen ("Events"). Hier werden Zustandsübergänge ausgelöst. Die Blöcke können beliebig oft definiert werden.
- **`goto(<State>)`** löst einen Übergang zum nächsten Zustand aus.
- **`stay()`** zeigt an, dass kein Zustandsübergang stattfindet. Die Daten können dennoch modifiziert werden.
- **`.using(<Data>)`** modifiziert die Daten für den nächsten Zustand.
