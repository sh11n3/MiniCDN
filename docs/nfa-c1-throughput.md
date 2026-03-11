# NFA-C1 – Durchsatznachweis (1 vs. 2 Edge-Instanzen)

## Ziel
Nachweis, dass bei **2 Edge-Instanzen** der Durchsatz mindestens **1,5×** gegenüber **1 Edge-Instanz** beträgt.

## Wo liegt das Skript?
- **Empfohlener Ort:** `scripts/performance/test-throughput-1v2.sh`
- **Kompatibler Root-Aufruf:** `./test-throughput-1v2.sh` (Wrapper, delegiert auf `scripts/performance/...`)

## Messprinzip
- Lastziel: `GET /api/cdn/files/{path}?region=EU` am Router.
- Erfolgreiche Requests: HTTP `307` (Router-Redirect).
- KPI: `RPS = successful_requests / elapsed_seconds`.
- Akzeptanzregel: `RPS_2 / RPS_1 >= 1.5`.

## Implementierte Ausführung
Das Skript führt reproduzierbar aus:
1. Services sicherstellen (startet sie bei Bedarf).
2. Edge-Executable-JAR sicherstellen (`edge/target/edge-1.0-SNAPSHOT-exec.jar`).
3. Baseline mit 1 Edge vorbereiten (EU → `http://localhost:8081`).
4. Testdatei am Origin hochladen.
5. Lastlauf mit 1 Edge.
6. Zweite Edge über Router-Lifecycle-Adapter starten und auto-registrieren.
7. Lastlauf mit 2 Edges.
8. Verhältnis berechnen und per Exit-Code bewerten.

## Wie führe ich sie aus? (IntelliJ-Terminal)
Du kannst beide Varianten verwenden:

```bash
./test-throughput-1v2.sh
```

oder direkt:

```bash
bash scripts/performance/test-throughput-1v2.sh
```

Mit Parametern:

```bash
DURATION_SEC=30 CONCURRENCY=60 WARMUP_REQUESTS=300 ./test-throughput-1v2.sh
```

## Wo sehe ich den Output?
- **Standard:** direkt live in der Konsole (inkl. Tabelle + PASS/FAIL).
- **Optional zusätzlich in Datei:**

```bash
./test-throughput-1v2.sh | tee throughput-run.log
```

Dann siehst du den Output in der Konsole **und** hast ihn in `throughput-run.log` gespeichert.

## CI-/Automations-Hinweis
- Der Nachweis ist ein **Performance-/Lasttest**, kein Unit-Test.
- Er sollte als separates Benchmark-Stage/Job laufen (nicht als flakey Unit-Test-Gate für jeden Commit).

## Warum kein Unit-Test?
Unit-Tests sind für funktionale Korrektheit einzelner Einheiten geeignet, aber nicht für systemischen Durchsatz unter Parallelität (Netzwerk, Scheduling, Prozessgrenzen).
