# NFA-C1 – Durchsatznachweis (1 vs. 2 Edge-Instanzen)

## Ziel
Nachweis, dass bei **2 Edge-Instanzen** der Durchsatz mindestens **1,5×** gegenüber **1 Edge-Instanz** beträgt.

## Wo liegt das Skript?
- **Hauptimplementierung (plattformunabhängig):** `scripts/performance/test_throughput_1v2.py`
- **Kompatible Einstiege:**
  - `python test-throughput-1v2.py`
  - `./test-throughput-1v2.sh` (delegiert intern auf Python)

## Messprinzip
- Lastziel: `GET /api/cdn/files/{path}?region=EU` am Router.
- Erfolgreiche Requests: HTTP `307` (Router-Redirect).
- KPI: `RPS = successful_requests / elapsed_seconds`.
- Akzeptanzregel: `RPS_2 / RPS_1 >= 1.5`.

## Implementierte Ausführung
Das Skript führt reproduzierbar aus:
1. Services sicherstellen (optional Autostart über `startup-service.sh`).
2. Edge-Executable-JAR sicherstellen (`edge/target/edge-1.0-SNAPSHOT-exec.jar`).
3. Baseline mit 1 Edge vorbereiten (EU → `http://localhost:8081`).
4. Testdatei am Origin hochladen.
5. Lastlauf mit 1 Edge.
6. Zweite Edge über Router-Lifecycle-Adapter starten und auto-registrieren.
7. Lastlauf mit 2 Edges.
8. Verhältnis berechnen und per Exit-Code bewerten.

## Wie führe ich es aus?

### Windows (PowerShell / IntelliJ Terminal)
```powershell
python .\test-throughput-1v2.py
```

### Linux/macOS
```bash
python3 ./test-throughput-1v2.py
```

### Parameter-Beispiel
```bash
python3 ./test-throughput-1v2.py --duration-sec 30 --concurrency 60 --warmup-requests 300
```

Alternativ über Umgebungsvariablen:
```bash
DURATION_SEC=30 CONCURRENCY=60 WARMUP_REQUESTS=300 python3 ./test-throughput-1v2.py
```

## Wo sehe ich den Output?
- **Standard:** direkt live in der Konsole (inkl. Tabelle + PASS/FAIL).
- **Optional zusätzlich in Datei:**

```bash
python3 ./test-throughput-1v2.py | tee throughput-run.log
```

## Wichtige Hinweise
- Standardmäßig startet das Skript Services **nicht automatisch**. Falls Router/Origin/Edge bereits laufen, ist keine weitere Aktion nötig.
- Optionaler Auto-Start (wenn Bash + `startup-service.sh` vorhanden):

```bash
AUTO_START_SERVICES=true python3 ./test-throughput-1v2.py
```

## CI-/Automations-Hinweis
- Der Nachweis ist ein **Performance-/Lasttest**, kein Unit-Test.
- Er sollte als separates Benchmark-Stage/Job laufen (nicht als flakey Unit-Test-Gate für jeden Commit).

## Warum kein Unit-Test?
Unit-Tests sind für funktionale Korrektheit einzelner Einheiten geeignet, aber nicht für systemischen Durchsatz unter Parallelität (Netzwerk, Scheduling, Prozessgrenzen).
