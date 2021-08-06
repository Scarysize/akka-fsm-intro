# `FSM` im Detail

```scala
class VendingMachine extends FSM[State, Int] {
  startWith(InitialState, 0)

  when(InitialState) {
    // ...
  }

  onTransition {
    case InitialState -> AddingCoins =>
      println("Cool, you started entering coins.")
      val nextSum: Int = nextStateData
      println(s"You entered $nextsum")
  }

  when(AddingCoins) {
    case Event(coins: Int, sum: Int) if (sum + coins) >= 100 =>
      goto(TransactionDone)
        .using(sum + coins)
        .forMax(3.seconds)

    case Event(coins: Int, sum: Int) =>
      stay().using(sum + coins)
  }

  onTransition {
    case AddingCoins -> TransactionDone =>
      dispenseSnickers()
  }

  when(TransactionDone) {
    case Event(StateTimeout, _) =>
      goto(InitialState).using(0)
  }
}
```

- **`onTransition`** Blöcke können Aktionen und Seiteneffekte bei Zustandsübergängen auslösen. Mehrere Blöcke können definiert werden. Pro Übergang werden alle Blöcke aufgerufen, nicht nur der erste passende.
- Im **`onTransition`** Block ist der letze und zukünftige Zustand verfügbar: `stateData` und `nextStateData`
- **`.forMax(<Duration>)`** setzt einen Timeout für eine Zustand. Nach der angegeben Zeit, wird ein `StateTimeout` Event ausgelöst.
