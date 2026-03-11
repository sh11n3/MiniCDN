# TODO – NFA-C1 Durchsatz (1 vs. 2 Edge-Instanzen)

## Plan
- [x] Anforderungen für NFA-C1 präzisieren (Messgröße, Vergleichslogik, Pass/Fail-Kriterium).
- [x] Reproduzierbares Lasttest-Skript für 1-vs-2-Instanzen implementieren.
- [x] Dokumentation für lokale Ausführung (IntelliJ-Terminal) ergänzen.
- [x] Verifizierung via Skript-/Build-Checks durchführen.
- [x] Review-Abschnitt mit Ergebnis und Grenzen ergänzen.
- [x] Strukturverbesserung nach Feedback: Skriptablage klarer machen und Ausführung/Output klar dokumentieren.

## Spezifikation (vor Implementierung)
- Messgröße: Erfolgreiche Requests pro Sekunde (RPS) gegen den Router-Endpunkt `/api/cdn/files/{path}?region=EU`.
- Zwei Läufe unter identischen Bedingungen:
  1. Lauf A mit genau 1 Edge-Instanz.
  2. Lauf B mit genau 2 Edge-Instanzen.
- Identische Lastparameter pro Lauf:
  - gleiche Dauer
  - gleiche Parallelität
  - gleiche Datei
  - gleicher Warmup
- Bestehensregel:
  - `RPS_2 / RPS_1 >= 1.5`
- Output:
  - tabellarische Kennzahlen je Lauf
  - Verhältnisfaktor
  - klarer Exit-Code (`0` bestanden, `1` nicht bestanden)

## Fortschritt
- [x] Anforderungen für NFA-C1 präzisieren (Messgröße, Vergleichslogik, Pass/Fail-Kriterium).
- [x] Lasttest-Skript erstellt.
- [x] Dokumentation erstellt.
- [x] Shell-Syntaxchecks für neue/berührte Skripte ausgeführt.
- [x] Skript nach `scripts/performance/` strukturiert, Root-Wrapper beibehalten.
- [x] Anleitung ergänzt: direkter Konsolen-Output und optionales Logging in Datei.

## Review
- Ergebnis: Skriptablage ist nun projektstrukturiert (`scripts/performance/test-throughput-1v2.sh`) bei gleichzeitiger Kompatibilität durch Root-Wrapper (`./test-throughput-1v2.sh`).
- Nutzerfragen „wo ablegen / wie ausführen / wo Output sichtbar“ sind explizit in der Doku beantwortet.
- Output ist standardmäßig live in der Konsole sichtbar; optional kann via `tee` in Datei protokolliert werden.
