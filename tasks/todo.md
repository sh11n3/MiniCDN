# TODO – NFA-C1 Durchsatz (1 vs. 2 Edge-Instanzen)

## Plan (aktuelle Korrektur: Bash -> Python)
- [x] Anforderungen für plattformunabhängige Ausführung (Windows/Linux/macOS) präzisieren.
- [x] Benchmark-Logik als Python-Skript mit Standardbibliothek umsetzen.
- [x] Bestehende Einstiegspfade kompatibel halten (Root-Aufruf + scripts/performance).
- [x] Dokumentation auf Python-Ausführung aktualisieren.
- [x] Verifizierungsschritte durchführen (Syntax + CLI-Help + Wrapper-Check).
- [x] Review-Abschnitt mit Ergebnis ergänzen.

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
- Plattformziel:
  - Ausführung per `python`/`python3` ohne Bash-Abhängigkeit.
- Output:
  - tabellarische Kennzahlen je Lauf
  - Verhältnisfaktor
  - klarer Exit-Code (`0` bestanden, `1` nicht bestanden)

## Fortschritt
- [x] Anforderungen für plattformunabhängige Ausführung (Windows/Linux/macOS) präzisiert.
- [x] Benchmark-Logik als Python-Skript mit Standardbibliothek umgesetzt.
- [x] Einstiegspfade kompatibel gehalten.
- [x] Dokumentation aktualisiert.
- [x] Verifizierung ausgeführt.

## Review
- Ergebnis: Die Hauptlogik liegt jetzt in `scripts/performance/test_throughput_1v2.py` und ist ohne Bash auf Windows, Linux und macOS ausführbar.
- Kompatibilität: Vorhandene `.sh`-Aufrufe bleiben als Wrapper erhalten, delegieren aber auf Python.
- Doku: Ausführung ist explizit für PowerShell (`python .\test-throughput-1v2.py`) und Unix (`python3 ./test-throughput-1v2.py`) dokumentiert.
- Verifiziert durch: Python-Kompilierung, CLI-Help-Ausgabe, Shell-Syntaxchecks der Wrapper.
